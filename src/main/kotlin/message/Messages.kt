package message

import java.time.Duration

interface Message {
    val instanceId: String
    val taskId: String?

}


abstract class ReceiveMessage : Message {

}

abstract class SendMessage : Message


data class UserFormMessage(val form: Map<String, Any?>, override val instanceId: String, override val taskId: String) :
    ReceiveMessage()

data class ScheduleTimerEvent(val duration: kotlin.time.Duration, override val instanceId: String, override val taskId: String) :
    SendMessage()




