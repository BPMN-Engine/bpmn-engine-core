package engine.database_connector.activity_event_log

import engine.storage_services.DatabaseConnector
import engine.storage_services.activity_event_log.models.LoggedEventDocument

enum class EventState {
    RUNNING, ERROR, FINISH,
}

interface ActivityEventLogService : DatabaseConnector {
    suspend fun addEvent(event: LoggedEventDocument)
    suspend fun getEvent(taskId: String, threadId: String, eventState: EventState? = null): LoggedEventDocument?
}