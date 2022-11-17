package engine.messaging.message

interface Message

interface InstanceMessage : Message {
    val modelId:String
}





interface TaskMessage : Message
interface EventMessage : Message



