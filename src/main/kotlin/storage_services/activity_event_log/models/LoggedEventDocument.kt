package engine.storage_services.activity_event_log.models

import engine.database_connector.activity_event_log.EventState
import engine.process_manager.models.Variables
import engine.storage_services.instance_log.InstanceId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class LoggedEventDocument(


    //generated id
    val _id: Id<String> = newId(),

    val instanceId: InstanceId,

    //element id in model
    val elementId: String,

    // variables at that event
    val variables: Variables?,

    //error
    val error: String?,

    //state of that event
    val eventState: EventState,

    //squential tasks would have the same id, if task diverges in gateway, all branches would get a new id
    val threadId: String,


    val creationTime: Long = Instant.now().toEpochMilli(),
)