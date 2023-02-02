package engine.messaging.receive_message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import engine.messaging.Message
import engine.messaging.task_message_service.messages.ServiceTaskMessage
import engine.messaging.task_message_service.messages.StartEventMessage
import engine.process_manager.models.Variables

abstract class ReceiveMessage : Message


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TaskStartMessage::class, name = "TaskStartMessage"),
    JsonSubTypes.Type(value = TaskCompleteMessage::class, name = "TaskCompleteMessage"),
    JsonSubTypes.Type(value = TaskFailedMessage::class, name = "TaskFailedMessage"),
)
abstract class TaskReceiveMessage(
    open val instanceId: String,
    open val taskId: String,
    open val threadId: String,
    open val elementId: String
) : ReceiveMessage()

data class TaskStartMessage(
    override val instanceId: String,
    override val taskId: String,
    override val threadId: String,
    override val elementId: String,
) : TaskReceiveMessage(
    instanceId,
    taskId,
    threadId,
    elementId,
)

data class TaskCompleteMessage(
    override val instanceId: String,
    override val taskId: String,
    val taskVariables: Variables,
    override val threadId: String,
    override val elementId: String,

    ) :
    TaskReceiveMessage(instanceId, taskId, threadId, elementId)

data class TaskFailedMessage(
    override val instanceId: String, override val taskId: String, val error: String, override val threadId: String,
    override val elementId: String,
) :
    TaskReceiveMessage(
        instanceId,
        taskId, threadId, elementId

    )