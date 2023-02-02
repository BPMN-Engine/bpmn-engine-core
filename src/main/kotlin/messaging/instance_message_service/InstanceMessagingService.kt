package engine.messaging.instance_message_service

import engine.messaging.MessagingService
import engine.messaging.instance_message_service.messages.InstanceMessage
import org.koin.core.component.KoinComponent

interface InstanceMessagingService : MessagingService, KoinComponent {
    suspend fun handleMessage(message: InstanceMessage)
}