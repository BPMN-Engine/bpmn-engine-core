package engine.messaging.task_message_service.messages

import engine.messaging.message.SendMessage
import engine.process_manager.models.Variables


abstract class TaskSendMessage(
    open val instanceId: String,
    open val taskId: String,
    open val sendVariables: Variables,
    open val threadId: String,
    open val elementId: String,

    ) : SendMessage()

//abstract class EventMessage(
//    open val variables: Variables
//) : SendMessage()
//
//
//data class StartEventMessage(override val variables: Variables) : EventMessage(variables)

data class StartEventMessage(
    override val sendVariables: Variables, override val instanceId: String,
    override val taskId: String,
    override val threadId: String,
    override val elementId: String

) : TaskSendMessage(instanceId, taskId, sendVariables, threadId, elementId)


data class ServiceTaskMessage(

    override val sendVariables: Variables, override val instanceId: String,
    override val taskId: String, override val threadId: String, override val elementId: String


) : TaskSendMessage(instanceId, taskId, sendVariables, threadId, elementId)