import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.storage_services.activity_event_log.models.LoggedEventDocument

class MockEventLogService : ActivityEventLogService {

    val log = mutableMapOf<String, MutableList<LoggedEventDocument>>()

    override suspend fun addEvent(event: LoggedEventDocument) {
//        log.putIfAbsent(instanceId, mutableListOf())
    }

    override suspend fun getEvent(taskId: String, threadId: String, eventState: EventState?): LoggedEventDocument? {
        return null
    }

    override suspend fun getEvent(taskId: String): LoggedEventDocument? {
        TODO("Not yet implemented")
    }

    override suspend fun getEvents(eventState: EventState?): List<LoggedEventDocument> {
        TODO("Not yet implemented")
    }

    override suspend fun connectToDatabase() {

    }


}



