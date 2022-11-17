package engine.shared

 import com.leo.service.RabbitSubscriptionService
 import engine.messaging.message.StartInstanceMessage
 import kotlinx.coroutines.flow.MutableSharedFlow
import engine.messaging.message.Message
import org.koin.core.qualifier.named
import org.koin.dsl.module


val koinModule = module {
    single(createdAtStart=false) { RabbitSubscriptionService().setupQueue() }
    single { MutableSharedFlow<Message>(extraBufferCapacity = 0) }
    single { MutableSharedFlow<StartInstanceMessage>(extraBufferCapacity = 0) }
}


