package engine.storage_services.instance_log

import engine.storage_services.DatabaseConnector
import engine.storage_services.instance_log.models.GetInstance
import engine.storage_services.thread_variables_log.ThreadVariablesLog

typealias InstanceId = String

interface InstanceLogService : ThreadVariablesLog, DatabaseConnector {
    suspend fun createInstance(modelId: String, processId: String): InstanceId
    suspend fun getInstance(instanceId: String): GetInstance

}


/*
* instance document has id(instance id), model id, name, variables subcollection,
* variable document has  id(thread id), variables
*
* */