package engine.messaging.task_message_service

import bpmn.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rabbitmq.client.*
import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.messaging.MessagingService
import engine.messaging.receive_message.TaskFailedMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.receive_message.TaskStartMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.process_manager.runners.TaskRunnerFactory
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@OptIn(DelicateCoroutinesApi::class)
class RabbitTaskMessagingService : TaskMessagingService {

    private val taskMessage: MutableSharedFlow<TaskSendMessage> by inject(named<TaskSendMessage>())
    private val activityEventLogService: ActivityEventLogService by inject()
    private val taskReceiveMessage: MutableSharedFlow<TaskReceiveMessage> by
        inject(named<TaskReceiveMessage>())

    @OptIn(ExperimentalCoroutinesApi::class)
    val taskExecutor = Dispatchers.IO.limitedParallelism(1000)

    private val connectionFactory: ConnectionFactory = ConnectionFactory()
    private lateinit var connection: Connection
    private lateinit var sendChannel: Channel
    private lateinit var receiveChannel: Channel
    companion object {

        val newq = -10

        val EXCHANGE = "task_exchange$newq"
        val QUEUE = "send_message_queue$newq"
        val REC_QUEUE = "rec_message_queue$newq"



    }

    init {
        println(EXCHANGE)
    }

    fun getChannel(): Channel {
        if (!sendChannel.isOpen) {
            println("sendChannel closed because of ${sendChannel.closeReason}")
            sendChannel = connection.createChannel()
        }
        return sendChannel
    }

    fun getReceiveChannel(): Channel {
        if (!receiveChannel.isOpen) {
            println("receiveChannel closed because of ${receiveChannel.closeReason}")
            receiveChannel = connection.createChannel()
        }
        return receiveChannel
    }
    override suspend fun setup(): MessagingService {
        connection = connectionFactory.newConnection()
        sendChannel = connection.createChannel()
        receiveChannel = connection.createChannel()

        setupSendChannel()

        setupReceiveChannel()

        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            GlobalScope.launch(taskExecutor) {
                try {
                    val message: TaskSendMessage = jsonMapper.readValue(delivery.body)
                    println("Task send  ${message.javaClass.simpleName}  ${message.elementId}")

                    getReceiveChannel()
                        .basicPublish(
                            EXCHANGE,
                            REC_QUEUE,
                            null,
                            jsonMapper.writeValueAsBytes(
                                TaskStartMessage(
                                    instanceId = message.instanceId,
                                    taskId = message.taskId,
                                    elementId = message.elementId,
                                    threadId = message.threadId,
                                )
                            )
                        )

                    val output = runBlocking {
                        GlobalScope.async { TaskRunnerFactory.createFromMessage(message) }.await()
                    }

                    getReceiveChannel()
                        .basicPublish(
                            EXCHANGE,
                            REC_QUEUE,
                            null,
                            jsonMapper.writeValueAsBytes(output)
                        )
                } catch (e: java.lang.Exception) {
                    println("deliverCallback exc $e")
                }
            }
        }
        GlobalScope.launch {
            getChannel().basicConsume(QUEUE, true, deliverCallback) { _: String? -> }
        }

        val deliverCallback2 = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            try {
                val message: TaskReceiveMessage = jsonMapper.readValue(delivery.body)
                println("Task receive ${message.javaClass.simpleName}  ${message.elementId}")
                GlobalScope.launch { handleMessage(message) }

                if(message is TaskFailedMessage){
                    println(message.error)
                }


            } catch (e: java.lang.Exception) {
                println("deliverCallback2 exc $e")
            }
        }
        GlobalScope.launch {
            getReceiveChannel().basicConsume(REC_QUEUE, true, deliverCallback2) { _: String? -> }
        }
        GlobalScope.launch {
            taskMessage
                .map { it }
                .flowOn(taskExecutor)
                .onEach { launch(taskExecutor) { dispatchMessage(it) } }
                .collect()
        }
        return this
    }

    private fun setupSendChannel() {
        try {
            getChannel().queueDeclarePassive(QUEUE)
        } catch (e: Exception) {
            getChannel().queueDeclare(QUEUE, true, false, false, emptyMap())
        }
        try {
            getChannel().exchangeDeclarePassive(EXCHANGE)
        } catch (e: Exception) {
            getChannel().exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT)
        }
        try {
            getChannel().queueBind(QUEUE, EXCHANGE, QUEUE)
        } catch (e: Exception) {
            println(e)
        }
    }

    private fun setupReceiveChannel() {
        try {
            getReceiveChannel().queueDeclarePassive(REC_QUEUE)
        } catch (e: Exception) {
            getReceiveChannel().queueDeclare(REC_QUEUE, true, false, false, emptyMap())
        }
        try {
            getReceiveChannel().exchangeDeclarePassive(EXCHANGE)
        } catch (e: Exception) {
            getReceiveChannel().exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT)
        }
        try {
            getReceiveChannel().queueBind(REC_QUEUE, EXCHANGE, REC_QUEUE)
        } catch (e: Exception) {
            println(e)
        }
    }

    override suspend fun dispatchMessage(message: TaskSendMessage) {
        getChannel().basicPublish(EXCHANGE, QUEUE, null, jsonMapper.writeValueAsBytes(message))
    }

    override suspend fun handleMessage(message: TaskReceiveMessage) {
        taskReceiveMessage.emit(message)
    }
}
