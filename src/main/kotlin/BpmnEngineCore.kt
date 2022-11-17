package engine


import engine.messaging.message.StartInstanceMessage
import engine.shared.koinModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named


fun main() {
    startKoin { modules(koinModule) }

    val core = BpmnEngineCore()

    runBlocking { core.setup() }

//    val model = BpmnParser.parse()
//    core.dispatchModel(model)
}


class BpmnEngineCore : KoinComponent {
    private val instanceInitiator: MutableSharedFlow<StartInstanceMessage> by inject()
//    private val rabbitSubscriptionService: RabbitSubscriptionService by inject()

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun setup() {
        println("Initialized core")

        GlobalScope.launch {
            instanceInitiator.emit(StartInstanceMessage(modelId = "test"))
         }

        instanceInitiator.onEach {
            val job = GlobalScope.async {
                 withContext(Dispatchers.Default) {   }
            }
            job.start()
        }.collect()


    }




}



