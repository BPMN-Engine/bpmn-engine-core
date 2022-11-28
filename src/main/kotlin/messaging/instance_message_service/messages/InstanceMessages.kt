package engine.messaging.instance_message_service.messages

import engine.messaging.message.SendMessage

abstract class InstanceMessage : SendMessage()

data class StartInstanceMessage(val modelId: String, val processId:String) : InstanceMessage()
