package engine.database_connector.activity_event_log


enum class EventState {
    START, RUNNING, ERROR, FINISH,
}

interface ActivityEventLogService {
    suspend fun addEvent(instanceId: String, eventId: String, eventState: EventState)
 }