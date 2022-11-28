package engine.messaging.instance_message_service

import engine.messaging.MessagingService
import engine.messaging.instance_message_service.messages.InstanceMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.inject

class MockInstanceMessagingService : InstanceMessagingService {
    private val instanceControlMessages: MutableSharedFlow<InstanceMessage> by inject()


    override suspend fun handleMessage(message: InstanceMessage) {
        instanceControlMessages.emit(message)
    }

    override suspend fun setup(): MessagingService {
        return this
    }

}