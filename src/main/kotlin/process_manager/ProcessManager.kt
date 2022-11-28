@file:OptIn(DelicateCoroutinesApi::class)

package engine.process_manager

import BpmnProcess
import DirectedElement
import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.messaging.instance_message_service.messages.InstanceMessage
import engine.messaging.instance_message_service.messages.StartInstanceMessage
import engine.messaging.receive_message.TaskCompleteMessage
import engine.messaging.receive_message.TaskFailedMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.receive_message.TaskStartMessage
import engine.messaging.task_message_service.messages.StartEventMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.parser.models.Event
import engine.parser.models.Gateway
import engine.parser.models.Task
import engine.process_manager.models.Variables
import engine.process_manager.tasks.Runnable
import engine.process_manager.tasks.RunnableEvent
import engine.process_manager.tasks.RunnableTask
import engine.storage_services.activity_event_log.models.LoggedEventDocument
import engine.storage_services.instance_log.InstanceId
import engine.storage_services.instance_log.InstanceLogService
import engine.storage_services.models_database.ModelsDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class ProcessManager : KoinComponent {
    private val modelsDatabase: ModelsDatabase by inject()
    private val activityLogService: ActivityEventLogService by inject()
    private val instanceLogService: InstanceLogService by inject()

    private val instanceControlMessages: MutableSharedFlow<InstanceMessage> by inject()
    private val taskMessages: MutableSharedFlow<TaskSendMessage> by inject(named<TaskSendMessage>())
    private val taskReceiveMessage: MutableSharedFlow<TaskReceiveMessage> by inject(named<TaskReceiveMessage>())

    private val instanceExecutor = Executors.newFixedThreadPool(180).asCoroutineDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val taskExecutor = Executors.newFixedThreadPool(1800).asCoroutineDispatcher()


    suspend fun setup(): Job {
        modelsDatabase.connectToDatabase()
        instanceLogService.connectToDatabase()
        activityLogService.connectToDatabase()
        setupTaskListener()

        return setupInstanceControlListener()


    }

    private fun setupInstanceControlListener(): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            instanceControlMessages.map { it }.flowOn(instanceExecutor).onEach {

                launch(instanceExecutor) {

                    if (it is StartInstanceMessage) {
                        val model = modelsDatabase.getModelById(it.modelId)

                        val process = model.getProcessById(it.processId)

                        val instanceId = instanceLogService.createInstance(it.modelId, it.processId)

                        val threadID = UUID.randomUUID().toString()

                        val startEvent = process.startEvent.first()

                        instanceLogService.saveVariables(mapOf(), threadID, instanceId)
                        taskMessages.emit(
                            StartEventMessage(
                                sendVariables = mapOf(),
                                instanceId = instanceId,
                                taskId = UUID.randomUUID().toString(),
                                threadId = threadID,
                                elementId = startEvent.id
                            )
                        )

                    }
                }


            }.collect()
        }
    }

    private fun setupTaskListener() {


        GlobalScope.launch(Dispatchers.IO) {
            taskReceiveMessage.onEach {
                launch(taskExecutor) {

             handleTaskReceiveMessage(it)


                }

            }.collect()
        }
    }


    private suspend fun generateTasks(
        elements: MutableList<DirectedElement>,
        instanceId: InstanceId,
        threadId: String,
        vars: Variables,
        process: BpmnProcess,
    ): MutableList<Runnable> {
        val runnableTasks = mutableListOf<Runnable>()


        instanceLogService.saveVariables(
            variables = vars,
            threadId = threadId,
            instanceId = instanceId
        )



        for (element in elements) {
            when (element) {
                is Gateway -> {
                    val previousElems = element.incoming?.map(process::sequenceFlowForId)?.map {
                        process.elementForId(it.sourceRef)
                    }?.toMutableList() ?: mutableListOf()


                    val states = previousElems.map {
                        activityLogService.getEvent(
                            it.id,
                            threadId,
                            eventState = EventState.FINISH
                        )
                    }

                    val canRun = !states.contains(null)

//                    println("Can run ${element.id} : $canRun")

                    if (canRun) {
                        val elems = element.outgoing?.map(process::sequenceFlowForId)?.map {
                            process.elementForId(it.targetRef)
                        }?.toMutableList() ?: mutableListOf()
                        runnableTasks.addAll(
                            generateTasks(
                                elements = elems,
                                instanceId = instanceId,
                                threadId = threadId,
                                vars = vars,
                                process = process
                            )
                        )
                    }

                }

                is Task -> {
                    runnableTasks.add(RunnableTask(threadId, element))
                }

                is Event -> {
                    runnableTasks.add(RunnableEvent(threadId, element))

                }
            }


        }

        return runnableTasks
    }


    private suspend fun handleTaskReceiveMessage(it: TaskReceiveMessage) {

        val instance = instanceLogService.getInstance(it.instanceId)

        val model = modelsDatabase.getModelById(instance.modelId)

        val process = model.getProcessById(instance.processId)


        val currentTask: DirectedElement = process.elementForId(it.elementId)

        var state = EventState.FINISH
        var vars: Variables? = null
        var error: String? = null



        when (it) {
            is TaskStartMessage -> {
                state = EventState.RUNNING
            }

            is TaskFailedMessage -> {
                error = it.error
                state = EventState.ERROR
            }


            is TaskCompleteMessage -> {
                //take thread variables and transform them

                if (currentTask.name!!.contains("end"))
                    println("Completed ${currentTask.name} at ${Instant.now()}")

                val threadVariables = instanceLogService.getVariables(it.threadId, it.instanceId)

                vars = threadVariables + it.taskVariables

                instanceLogService.saveVariables(
                    variables = vars,
                    threadId = it.threadId,
                    instanceId = it.instanceId
                )


            }
        }


        activityLogService.addEvent(
            LoggedEventDocument(
                elementId = it.elementId,
                variables = vars,
                error = error,
                eventState = state,
                threadId = it.threadId,
                instanceId = it.instanceId,
            )
        )

        if (it is TaskCompleteMessage) {

            val nextElements =
                (currentTask.outgoing?.map { process.sequenceFlowForId(it) }?.map {
                    process.elementForId(it.targetRef)
                })?.toMutableList() ?: mutableListOf()


            val allRunnableElements: MutableList<Runnable> =
                generateTasks(
                    elements = nextElements,
                    instanceId = it.instanceId,
                    threadId = it.threadId,
                    vars = vars ?: mutableMapOf(),
                    process = process
                )


            for (el in allRunnableElements) {

                GlobalScope.launch(taskExecutor) {

//                    val variables = instanceLogService.getVariables(
//                        threadId = el.threadId,
//                        instanceId = it.instanceId
//                    )

                    taskMessages.emit(
                        StartEventMessage(
                            sendVariables = vars!!,
                            instanceId = it.instanceId,
                            taskId = UUID.randomUUID().toString(),
                            threadId = el.threadId,
                            elementId = el.runnable.id
                        )
                    )
//                    println("Sent ${el.runnable.name}")


                }

            }
        }


    }

}


