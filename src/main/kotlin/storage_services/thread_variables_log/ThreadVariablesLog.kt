package engine.storage_services.thread_variables_log

import engine.process_manager.models.Variables

interface ThreadVariablesLog {
    suspend fun saveVariables(variables: Variables, threadId: String, instanceId: String)
    suspend fun getVariables(threadId: String, instanceId: String): Variables
}