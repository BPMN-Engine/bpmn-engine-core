import bpmn.BpmnModel
import bpmn.BpmnParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import message.Message
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import workflow.BpmnWorkflow


val koinModule = module {
    single { MutableSharedFlow<Message>(extraBufferCapacity = 1) }
    single(qualifier = named("modelFlow")) { MutableSharedFlow<BpmnModel>(extraBufferCapacity = 0) }

}


fun main() {
    val core = BpmnEngineCore()
    GlobalScope.launch {
        delay(100)
        core.dispatchModel(BpmnParser.parse())
    }
    runBlocking { core.setup() }

//    val model = BpmnParser.parse()
//    core.dispatchModel(model)
}


class BpmnEngineCore : KoinComponent {
    private val instanceInitiator: MutableSharedFlow<BpmnModel> by inject(qualifier = named("modelFlow"))


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun setup() {
        println("Initialized core")
        startKoin { modules(koinModule) }


        instanceInitiator.onEach {
            val job = GlobalScope.async {
                val workflow: BpmnWorkflow = BpmnWorkflow()
                withContext(Dispatchers.Default) { workflow.runModel(it) }
            }
            job.start()
        }.collect()


    }


    suspend fun dispatchModel(bpmnModel: BpmnModel) {

        instanceInitiator.emit(bpmnModel)

    }

}



