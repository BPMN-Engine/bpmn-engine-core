package engine.process_manager.message_handlers

import engine.messaging.Message
import engine.messaging.instance_message_service.messages.InstanceMessage
import engine.messaging.instance_message_service.messages.StartInstanceMessage
import org.koin.core.component.KoinComponent

interface MessageHandler {
    val message: Message
    suspend fun handle():String


}

interface InstanceMessageHandler : MessageHandler, KoinComponent {

    override val message: InstanceMessage
}

object MessageHandlerFactory {
    fun fromMessage(message: Message): MessageHandler {
        return when (message) {
            is StartInstanceMessage -> StartInstanceMessageHandler(message = message)
            else -> throw NotImplementedError("Not implemented for $message")
        }
    }

}
