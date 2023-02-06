import bpmn.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rabbitmq.client.*
import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.messaging.instance_message_service.messages.StartInstanceMessage
import engine.messaging.receive_message.TaskCompleteMessage
import engine.messaging.task_message_service.RabbitTaskMessagingService
import engine.messaging.task_message_service.TaskMessagingService
import engine.parser.models.UserTask
import engine.process_manager.ProcessManager
import engine.process_manager.message_handlers.MessageHandlerFactory
import engine.process_manager.models.Variables
import engine.storage_services.activity_event_log.models.LoggedEventDocument
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RPCServer : KoinComponent {

    private val processManager: ProcessManager by inject()

    private val RPC_QUEUE_NAME = "rpc_queue"

 
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun setup() {
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null)
        channel.queuePurge(RPC_QUEUE_NAME)
        channel.basicQos(1)
        println(" [x] Awaiting RPC requests")
        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            GlobalScope.launch {
                val replyProps =
                    AMQP.BasicProperties.Builder()
                        .correlationId(delivery.properties.correlationId)
                        .build()
                var response = ""
                try {
                    val message: Map<String, Any> =
                        jsonMapper.readValue(delivery.body.decodeToString())

                    response = jsonMapper.writeValueAsString(handleMessage(message))
                } catch (e: RuntimeException) {
                    println(" [.] $e")
                } finally {
                    channel.basicPublish(
                        "",
                        delivery.properties.replyTo,
                        replyProps,
                        response.toByteArray(charset("UTF-8"))
                    )
                    channel.basicAck(delivery.envelope.deliveryTag, false)
                }
            }
        }
        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback) { consumerTag: String? -> }
    }

    private suspend fun handleMessage(message: Map<String, Any>): Map<String, Any> {
        val type = message["type"].toString()
        when (type) {
            "runningUserTasks" -> {
                val tasks =
                    processManager.getRunningTasks().filter {
                        it.eventState == EventState.RUNNING || it.eventState == EventState.FINISH
                    }


                val mappedTasks =
                    tasks
                        .map {
                            mapOf(
                                "instanceId" to it.instanceId,
                                "id" to it._id,
                                "task" to processManager.getElementFromLogItem<DirectedElement>(it)
                            )
                        }
                        .toMutableList()
                        .filter { it["task"] is UserTask }

                return mapOf("tasks" to (mappedTasks))
            }
            "createInstance" -> {
                val id =
                    MessageHandlerFactory.fromMessage(
                            StartInstanceMessage(
                                message["modelId"].toString(),
                                message["processId"].toString()
                            )
                        )
                        .handle()
                return mapOf("id" to id)
            }
            "completeTask" -> {

                val eventLogService: ActivityEventLogService by inject()

                val taskId = message["taskId"].toString()
                val logElement = eventLogService.getEvent(taskId = taskId)!!
                val formValues = message["values"] as Variables

                try {
                    val messagingService: TaskMessagingService by inject()
                    val message =
                        TaskCompleteMessage(
                            instanceId = logElement.instanceId,
                            taskId = taskId,
                            taskVariables = formValues,
                            threadId = logElement.threadId,
                            elementId = logElement.elementId
                        )

                    (messagingService as RabbitTaskMessagingService)
                        .getReceiveChannel()
                        .basicPublish(
                            RabbitTaskMessagingService.EXCHANGE,
                            RabbitTaskMessagingService.REC_QUEUE,
                            null,
                            jsonMapper.writeValueAsBytes(message)
                        )
                } catch (e: Exception) {
                    println(e)
                }
            }
            "formData" -> {
                val taskId = message["taskId"].toString()
                try {
                    val task = processManager.getElement<UserTask>(taskId)
                    return task.userTaskForm ?: mapOf()
                } catch (e: Exception) {}
            }
        }
        return mapOf()
    }
}
