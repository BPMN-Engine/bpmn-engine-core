package engine.database_connector.instance_log

import java.util.*

class MockInstanceLog : InstanceLog {

    private val instanceLog = mutableMapOf<String, String>()


    override suspend fun createInstance(modelName: String): UUID {
        val id = UUID.randomUUID()
        instanceLog[id.toString()] = modelName
        return id
    }
}