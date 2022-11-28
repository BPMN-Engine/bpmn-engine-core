package engine.messaging.task_message_service

import engine.messaging.MessagingService
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import org.koin.core.component.KoinComponent

interface TaskMessagingService : MessagingService, KoinComponent {
    suspend fun dispatchMessage(message: TaskSendMessage)
    suspend fun handleMessage(message: TaskReceiveMessage)
}