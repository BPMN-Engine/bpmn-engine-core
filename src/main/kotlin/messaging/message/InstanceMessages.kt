package engine.messaging.message

import engine.messaging.message.InstanceMessage

data class StartInstanceMessage(override val modelId: String) : InstanceMessage