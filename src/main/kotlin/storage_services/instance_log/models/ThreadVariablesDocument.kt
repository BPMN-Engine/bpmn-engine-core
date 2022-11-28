package engine.storage_services.instance_log.models

import engine.process_manager.models.Variables
import engine.storage_services.instance_log.InstanceId

data class ThreadVariablesDocument(
    val instanceId: InstanceId, val variables: Variables, val threadId: String,
)