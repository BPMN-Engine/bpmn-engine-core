import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import java.time.Instant

class MockEventLogService : ActivityEventLogService {

    val log = mutableMapOf<String, MutableList<LoggedEvent>>()

    override suspend fun addEvent(instanceId: String, eventId: String, eventState: EventState) {
        log.putIfAbsent(instanceId, mutableListOf())
        log[instanceId]?.add(LoggedEvent(eventId, "", "", mutableMapOf(), eventState))
    }


}


data class LoggedEvent(


    //generated id
    val id: String,

    //referencing id
    val previousId: String,

    //internal id in modeler
    val nextTaskId: String,

    // variables at that event
    val variables: Map<String, Any?>,

    val eventState: EventState,


    val creationTime: Long = Instant.now().toEpochMilli(),
)