package engine.storage_services.instance_log

import engine.process_manager.models.Variables
import engine.storage_services.instance_log.models.GetInstance
import engine.storage_services.thread_variables_log.ThreadId
import java.util.*

class MockInstanceLogService : InstanceLogService {

    private val instanceLog = mutableMapOf<String, String>()
    private val threadLog = mutableMapOf<String, Variables>()


    override suspend fun createInstance(modelId: String, processId: String): String {
        val id = UUID.randomUUID()
        instanceLog[id.toString()] = modelId
        return id.toString()
    }

    override suspend fun getInstance(instanceId: String): GetInstance {
        return GetInstance(modelId = "", processId = "", id = "")
    }

    override suspend fun saveVariables(variables: Variables, instanceId: String, threadId: String?): ThreadId {
        threadLog[threadId!!] = variables
        return ""
    }


    override suspend fun getVariables(threadId: String, instanceId: String): Variables {

        return threadLog[threadId]!!

    }

    override suspend fun connectToDatabase() {
    }
}