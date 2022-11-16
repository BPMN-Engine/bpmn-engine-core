package engine


import engine.parser.models.BpmnModel
import bpmn.BpmnParser
import engine.shared.koinModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import engine.process_manager.workers.BpmnWorkflowWorker


fun main() {
    startKoin { modules(koinModule) }

    val core = BpmnEngineCore()

    runBlocking { core.setup() }

//    val model = BpmnParser.parse()
//    core.dispatchModel(model)
}


class BpmnEngineCore : KoinComponent {
    private val instanceInitiator: MutableSharedFlow<BpmnModel> by inject(qualifier = named("modelFlow"))
//    private val rabbitSubscriptionService: RabbitSubscriptionService by inject()

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun setup() {
        println("Initialized core")

        GlobalScope.launch {
            delay(100)
            instanceInitiator.emit(BpmnParser.parse())
//            rabbitSubscriptionService.startListening();
        }

        instanceInitiator.onEach {
            val job = GlobalScope.async {
                val workflow: BpmnWorkflowWorker = BpmnWorkflowWorker()
                withContext(Dispatchers.Default) { workflow.createInstance(it) }
            }
            job.start()
        }.collect()


    }




}



