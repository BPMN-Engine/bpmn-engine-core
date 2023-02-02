package engine.storage_services.thread_variables_log

import engine.process_manager.models.Variables

typealias ThreadId = String

interface ThreadVariablesLog {
    suspend fun saveVariables(variables: Variables, instanceId: String, threadId: String? = null): ThreadId
    suspend fun getVariables(threadId: String, instanceId: String): Variables
}