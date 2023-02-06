package engine.messaging.task_message_service.messages

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import engine.messaging.message.SendMessage
import engine.process_manager.models.Variables

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = StartEventMessage::class, name = "StartEventMessage"),
    JsonSubTypes.Type(value = ServiceTaskMessage::class, name = "ServiceTaskMessage"),
    JsonSubTypes.Type(value = UserFromMessage::class, name = "UserFromMessage"),
)
abstract class TaskSendMessage(
    open val instanceId: String,
    open val taskId: String,
    open val sendVariables: Variables,
    open val threadId: String,
    open val elementId: String,
) : SendMessage()

// abstract class EventMessage(
//    open val variables: Variables
// ) : SendMessage()
//
//
// data class StartEventMessage(override val variables: Variables) : EventMessage(variables)

data class StartEventMessage(
    override val sendVariables: Variables,
    override val instanceId: String,
    override val taskId: String,
    override val threadId: String,
    override val elementId: String
) : TaskSendMessage(instanceId, taskId, sendVariables, threadId, elementId)
data class UserFromMessage(
    override val sendVariables: Variables,
    override val instanceId: String,
    override val taskId: String,
    override val threadId: String,
    override val elementId: String
) : TaskSendMessage(instanceId, taskId, sendVariables, threadId, elementId)

data class ServiceTaskMessage(
    override val sendVariables: Variables,
    override val instanceId: String,
    override val taskId: String,
    override val threadId: String,
    override val elementId: String,
    val url: String,
    val method: String = "GET"
) : TaskSendMessage(instanceId, taskId, sendVariables, threadId, elementId)
