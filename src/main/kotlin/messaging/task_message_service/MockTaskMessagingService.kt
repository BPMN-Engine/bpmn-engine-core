package engine.messaging.task_message_service

import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.messaging.MessagingService
import engine.messaging.receive_message.TaskCompleteMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.receive_message.TaskStartMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.concurrent.Executors

@OptIn(DelicateCoroutinesApi::class)
class MockTaskMessagingService : TaskMessagingService {


    private val taskMessage: MutableSharedFlow<TaskSendMessage> by inject(named<TaskSendMessage>())
    private val activityEventLogService: ActivityEventLogService by inject()
    private val taskReceiveMessage: MutableSharedFlow<TaskReceiveMessage> by inject(named<TaskReceiveMessage>())
    val taskExecutor = Dispatchers.IO.limitedParallelism(1800)
    override suspend fun setup(): MessagingService {
        GlobalScope.launch {
            taskMessage.map { it }.flowOn(taskExecutor).onEach {
          launch(taskExecutor) {     dispatchMessage(it) }
            }.collect()
        }
        return this


    }

     override suspend fun dispatchMessage(message: TaskSendMessage) {
        // dispatch to rmq queue, for mocking purpose autohandle it



             delay(100)
             handleMessage(
                 TaskStartMessage(
                     instanceId = message.instanceId,
                     taskId = message.taskId,
                     elementId = message.elementId,
                     threadId = message.threadId,
                 )
             )
             delay(100)
             handleMessage(
                 TaskCompleteMessage(
                     instanceId = message.instanceId,
                     taskId = message.taskId,
                     taskVariables = mutableMapOf("testkey" to "key"),
                     elementId = message.elementId,
                     threadId = message.threadId,

                     )
             )



    }

    override suspend fun handleMessage(message: TaskReceiveMessage) {

            taskReceiveMessage.emit(message)

    }


}