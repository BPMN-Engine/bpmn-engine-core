package engine.messaging.receive_message

import engine.process_manager.models.Variables

abstract class ReceiveMessage

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