package engine.storage_services.instance_log.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


abstract class Instance(open val modelId: String, open val processId: String)


data class InstanceDocument(
    override val modelId: String,
    override val processId: String,
    @BsonId
    var _id: ObjectId?
) : Instance(modelId, processId)

data class GetInstance(override val modelId: String, override val processId: String, val id: String) :
    Instance(modelId, processId)