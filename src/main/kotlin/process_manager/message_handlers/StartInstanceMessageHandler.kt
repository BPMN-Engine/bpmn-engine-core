package engine.process_manager.message_handlers

import engine.messaging.instance_message_service.messages.StartInstanceMessage
import engine.messaging.task_message_service.messages.StartEventMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.storage_services.instance_log.InstanceLogService
import engine.storage_services.models_database.ModelsDatabase
import engine.util.generateUUID
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class StartInstanceMessageHandler(override val message: StartInstanceMessage) : InstanceMessageHandler {

    private val modelsDatabase: ModelsDatabase by inject()
    private val instanceLogService: InstanceLogService by inject()
    private val taskMessages: MutableSharedFlow<TaskSendMessage> by inject(named<TaskSendMessage>())


    override suspend fun handle() {
        val it = message

        val model = modelsDatabase.getModelById(it.modelId)

        val process = model.getProcessById(it.processId)

        val instanceId = instanceLogService.createInstance(it.modelId, it.processId)

        val startEvent = process.startEvent.first()

        val threadID = instanceLogService.saveVariables(mapOf(), instanceId)
        taskMessages.emit(
            StartEventMessage(
                sendVariables = mapOf(),
                instanceId = instanceId,
                taskId = generateUUID(),
                threadId = threadID,
                elementId = startEvent.id
            )
        )

    }


}