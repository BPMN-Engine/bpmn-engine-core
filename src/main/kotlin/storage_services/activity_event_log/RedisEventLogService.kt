package engine.storage_services.activity_event_log

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.process_manager.taskExecutor
import engine.storage_services.activity_event_log.models.LoggedEventDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol
import redis.clients.jedis.search.*

class RedisEventLogService : ActivityEventLogService, KoinComponent {

    lateinit var db: JedisPooled
    val jsonMapper = ObjectMapper().registerKotlinModule()

    override suspend fun addEvent(event: LoggedEventDocument) {

        val value =
            withContext(Dispatchers.IO) {
                db.jsonSet("event:" + event._id, jsonMapper.writeValueAsString(event))
                // delete running
                if (event.eventState == EventState.FINISH) {
                    val running = getEvent(event.elementId, event.threadId)
                    if (running != null) db.del("event:" + running._id)
                }
            }

        //        logCol.insertOne(event)
    }

    override suspend fun getEvent(
        elementId: String,
        threadId: String,
        eventState: EventState?
    ): LoggedEventDocument? {

        val value =
            GlobalScope.async(taskExecutor) {
                    //            @\$\.threadId:$threadId*
                    val q = Query("@\\$\\.elementId:$elementId* @\\$\\.eventState:$eventState*")

                    val search: SearchResult = db.ftSearch("event-index", q)
                    val docs: List<Document> = search.documents

                    if (docs.isEmpty()) {
                        return@async null
                    }
                    val first = docs.first()

                    val obj: LoggedEventDocument = jsonMapper.readValue(first["$"].toString())

                    return@async obj
                }
                .await()

        return value
    }

    override suspend fun getEvent(taskId: String): LoggedEventDocument? {

        val value =
            GlobalScope.async(taskExecutor) {
                    //            @\$\.threadId:$threadId*
                    val q = Query("@\\$\\.:$taskId")

                    val search: SearchResult = db.ftSearch("event-index", q)
                    val docs: List<Document> = search.documents

                    if (docs.isEmpty()) {
                        return@async null
                    }
                    val first = docs.first()

                    val obj: LoggedEventDocument = jsonMapper.readValue(first["$"].toString())

                    return@async obj
                }
                .await()

        return value
    }

    override suspend fun getEvents(eventState: EventState?): List<LoggedEventDocument> {

        val value =
            GlobalScope.async(taskExecutor) {
                    //            @\$\.threadId:$threadId*
                    val q = Query("@\\$\\.eventState:$eventState*").limit(0, 10000)

                    val search: SearchResult = db.ftSearch("event-index", q)
                    val docs: List<Document> = search.documents

                    if (docs.isEmpty()) {
                        return@async null
                    }

                    docs.map { jsonMapper.readValue<LoggedEventDocument>(it["$"].toString()) }
                }
                .await()

        return value ?: mutableListOf()
    }

    override suspend fun connectToDatabase() {
        db = JedisPooled("localhost", 6379)
        db.sendCommand(Protocol.Command.FLUSHALL)
        val schema: Schema =
            Schema()
                .addTextField("$._id", 1.0)
                .addTextField("\$.threadId", 1.0)
                .addTextField("\$.eventState", 1.0)
                .addTextField("\$.elementId", 1.0)

        val rule: IndexDefinition = IndexDefinition(IndexDefinition.Type.JSON).setPrefixes("event:")

        try {
            db.ftCreate("event-index", IndexOptions.defaultOptions().setDefinition(rule), schema)
        } catch (e: Exception) {
            println("Exception")
        }
    }
}
