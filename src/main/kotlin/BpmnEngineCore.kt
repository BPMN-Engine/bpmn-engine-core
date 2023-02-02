package engine


import engine.messaging.instance_message_service.InstanceMessagingService
import engine.messaging.instance_message_service.messages.StartInstanceMessage
import engine.messaging.task_message_service.TaskMessagingService
import engine.process_manager.ProcessManager
import engine.shared.koinModule
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.time.Instant
import java.util.*
import kotlin.system.measureTimeMillis

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    startKoin { modules(koinModule) }
    val core = BpmnEngineCore()
    core.run()
}



class BpmnEngineCore : KoinComponent {

    private val processManager: ProcessManager by inject()
    private val instanceMessagingService: InstanceMessagingService by inject()
    private val taskMessagingService: TaskMessagingService by inject()

    suspend fun run() {
        val job = processManager.setup()
        job.start()

        instanceMessagingService.setup()
        taskMessagingService.setup()

//        GlobalScope.launch {
//            delay(100)
//
//            val duration = measureTimeMillis {
//                repeat(1) {
//                    println(Instant.now().toEpochMilli())
//                    instanceMessagingService.handleMessage(
//                        StartInstanceMessage(
//                            modelId = "test",
//                            processId = "Process_0c2yfbx"
//                        )
//                    )
//
//                }
//            }
//
//
//        }


        job.join()
        /*

        logged event
        {
        "id" : unique id
        "instanceId" : id of that instance
        "taskId":"id from model"
        "variables": key value
        "error":null
        "branchId" - changes on gateway

        }





        send start event


        each event should have ID of previous task from database



        receive started event -> save in db


        receive completed event -> save in db

        lookup main models db and find next task

       completedEvent.taskId...find...outgoing forEach dispath start event



        new task will have






        */


    }


}



