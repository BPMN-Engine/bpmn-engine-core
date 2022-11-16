package engine.shared

import engine.parser.models.BpmnModel
import com.leo.service.RabbitSubscriptionService
import engine.database_connector.models_database.MockModelsDatabase
import engine.database_connector.models_database.ModelsDatabase
import kotlinx.coroutines.flow.MutableSharedFlow
import message.Message
import org.koin.core.qualifier.named
import org.koin.dsl.module


val koinModule = module {
    single<ModelsDatabase>(createdAtStart=true) { MockModelsDatabase() }
    single(createdAtStart=false) { RabbitSubscriptionService().setupQueue() }
    single { MutableSharedFlow<Message>(extraBufferCapacity = 0) }
    single(qualifier = named("modelFlow")) { MutableSharedFlow<BpmnModel>(extraBufferCapacity = 0) }
}


