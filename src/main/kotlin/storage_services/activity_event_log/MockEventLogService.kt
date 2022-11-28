import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.process_manager.models.Variables
import engine.storage_services.activity_event_log.models.LoggedEventDocument
import java.time.Instant

class MockEventLogService : ActivityEventLogService {

    val log = mutableMapOf<String, MutableList<LoggedEventDocument>>()

    override suspend fun addEvent(  event: LoggedEventDocument) {
//        log.putIfAbsent(instanceId, mutableListOf())
    }

    override suspend fun getEvent(taskId: String, threadId: String, eventState: EventState?): LoggedEventDocument? {
    return null
    }

    override suspend fun connectToDatabase() {

    }


}



