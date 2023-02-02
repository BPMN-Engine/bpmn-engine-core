package engine.shared

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.models_database.MockModelsDatabase
import engine.messaging.instance_message_service.InstanceMessagingService
import engine.messaging.instance_message_service.MockInstanceMessagingService
import engine.messaging.instance_message_service.messages.InstanceMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.task_message_service.MockTaskMessagingService
import engine.messaging.task_message_service.RabbitTaskMessagingService
import engine.messaging.task_message_service.TaskMessagingService
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.process_manager.ProcessManager
import engine.storage_services.activity_event_log.RedisEventLogService
import engine.storage_services.instance_log.InstanceLogService
import engine.storage_services.instance_log.MongoInstanceLogService
import engine.storage_services.models_database.ModelsDatabase
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val string = "mongodb://root:password@localhost:27017"


val koinModule = module {
    single<InstanceLogService>(createdAtStart = false) { (MongoInstanceLogService()) }
    single<TaskMessagingService>(createdAtStart = false) { (RabbitTaskMessagingService()) }
    single<ActivityEventLogService>(createdAtStart = false) { (RedisEventLogService()) }
    single<InstanceMessagingService>(createdAtStart = false) { MockInstanceMessagingService() }
    single<ModelsDatabase>(createdAtStart = false) { MockModelsDatabase() }
    single { MutableSharedFlow<InstanceMessage>(extraBufferCapacity = 0) }
    single(named<TaskSendMessage>()) { MutableSharedFlow<TaskSendMessage>(extraBufferCapacity = 0) }
    single(named<TaskReceiveMessage>()) { MutableSharedFlow<TaskReceiveMessage>(extraBufferCapacity = 0) }
    single { ProcessManager() }

    single<CoroutineClient>(createdAtStart = true) {
        KMongo.createClient(
            MongoClientSettings.builder().applyConnectionString(ConnectionString(string))
                .applyToConnectionPoolSettings {
                    it.minSize(150).maxSize(2000)
                        .build()
                }.build()
        ).coroutine
    }

}

