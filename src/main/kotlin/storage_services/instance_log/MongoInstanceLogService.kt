package engine.storage_services.instance_log

import engine.process_manager.models.Variables
import engine.storage_services.instance_log.models.GetInstance
import engine.storage_services.instance_log.models.InstanceDocument
import engine.storage_services.instance_log.models.ThreadVariablesDocument
import engine.storage_services.thread_variables_log.ThreadId
import org.bson.types.ObjectId
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoInstanceLogService : InstanceLogService, KoinComponent {
    lateinit var db: CoroutineDatabase

    private lateinit var instanceCol: CoroutineCollection<InstanceDocument>
    private val threadCol by lazy { db.getCollection<ThreadVariablesDocument>() }


    override suspend fun createInstance(modelId: String, processId: String): InstanceId {
        val doc = InstanceDocument(modelId, processId, null)
        instanceCol.insertOne(doc)

        return doc._id.toString()
    }

    override suspend fun getInstance(instanceId: String): GetInstance {

        val doc = instanceCol.findOne(InstanceDocument::_id eq ObjectId(instanceId))!!

        return GetInstance(doc.modelId, doc.processId, instanceId)
    }

    override suspend fun saveVariables(variables: Variables, instanceId: String, threadId: String?): ThreadId {


        if (threadId != null) {
            threadCol.save(
                ThreadVariablesDocument(
                    _id = ObjectId(threadId),
                    instanceId = instanceId,
                    variables = variables,
                )
            )
        } else {
            val doc = ThreadVariablesDocument(
                instanceId = instanceId,
                variables = variables,
            )
            threadCol.save(
                doc
            )
            return doc._id.toString()
        }


        return threadId
    }

    override suspend fun getVariables(threadId: String, instanceId: String): Variables {
        val doc = threadCol.findOne(
            ThreadVariablesDocument::instanceId eq instanceId,
            ThreadVariablesDocument::_id eq ObjectId(threadId)
        )
        return doc?.variables ?: mutableMapOf()
    }

    override suspend fun connectToDatabase() {
        val client: CoroutineClient by inject()

        db = client.getDatabase("instances_db")
        instanceCol = db.getCollection<InstanceDocument>()

//        db.createCollection("instances")

    }
}