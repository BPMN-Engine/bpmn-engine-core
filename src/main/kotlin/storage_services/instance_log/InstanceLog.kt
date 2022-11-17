package engine.database_connector.instance_log

import java.util.*

interface InstanceLog {
    suspend fun createInstance(modelName: String): UUID

}