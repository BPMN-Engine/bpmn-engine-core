package engine.storage_services.activity_event_log

import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.storage_services.activity_event_log.models.LoggedEventDocument
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoEventLogService : ActivityEventLogService, KoinComponent {

    lateinit var db: CoroutineDatabase
    private val logCol by lazy { db.getCollection<LoggedEventDocument>("event_log") }
    private val mutex = Mutex()
    override suspend fun addEvent(event: LoggedEventDocument) {
         logCol.insertOne(event)
    }

    override suspend fun getEvent(
        taskId: String,
        threadId: String,
        eventState: EventState?
    ): LoggedEventDocument? {
        val d = if (eventState == null) null else LoggedEventDocument::eventState eq eventState
        return logCol.findOne(
            LoggedEventDocument::elementId eq taskId,
            LoggedEventDocument::threadId eq threadId,
            d,
        )
    }

    override suspend fun connectToDatabase() {
        val client: CoroutineClient by inject()

        db = client.getDatabase("instances_db")
    }
}
