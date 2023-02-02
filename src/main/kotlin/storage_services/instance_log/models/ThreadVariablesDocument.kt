package engine.storage_services.instance_log.models

import engine.process_manager.models.Variables
import engine.storage_services.instance_log.InstanceId
import org.bson.types.ObjectId
import org.litote.kmongo.newId

data class ThreadVariablesDocument(
    val _id: ObjectId = ObjectId(newId<String>().toString()),
    val instanceId: InstanceId, val variables: Variables,
)