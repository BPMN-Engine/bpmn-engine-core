@file:OptIn(DelicateCoroutinesApi::class)

package engine.process_manager

import BpmnProcess
import DirectedElement
import Mapping
import engine.database_connector.activity_event_log.ActivityEventLogService
import engine.database_connector.activity_event_log.EventState
import engine.messaging.instance_message_service.messages.InstanceMessage
import engine.messaging.receive_message.TaskCompleteMessage
import engine.messaging.receive_message.TaskFailedMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.receive_message.TaskStartMessage
import engine.messaging.task_message_service.messages.ServiceTaskMessage
import engine.messaging.task_message_service.messages.StartEventMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.messaging.task_message_service.messages.UserFromMessage
import engine.parser.models.*
import engine.process_manager.message_handlers.MessageHandlerFactory
import engine.process_manager.models.Variables
import engine.process_manager.tasks.Runnable
import engine.process_manager.tasks.RunnableEvent
import engine.process_manager.tasks.RunnableTask
import engine.storage_services.activity_event_log.models.LoggedEventDocument
import engine.storage_services.instance_log.InstanceId
import engine.storage_services.instance_log.InstanceLogService
import engine.storage_services.models_database.ModelsDatabase
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

private val semaphoreMap = ConcurrentHashMap<String, Semaphore>()
val taskExecutor = Executors.newFixedThreadPool(1000).asCoroutineDispatcher()

class ProcessManager : KoinComponent {
    private val modelsDatabase: ModelsDatabase by inject()
    private val activityLogService: ActivityEventLogService by inject()
    private val instanceLogService: InstanceLogService by inject()

    private val instanceControlMessages: MutableSharedFlow<InstanceMessage> by inject()
    private val taskMessages: MutableSharedFlow<TaskSendMessage> by inject(named<TaskSendMessage>())
    private val taskReceiveMessage: MutableSharedFlow<TaskReceiveMessage> by
        inject(named<TaskReceiveMessage>())

    private val instanceExecutor = Executors.newFixedThreadPool(1000).asCoroutineDispatcher()

    suspend fun setup(): Job {
        setupDatabases()
        setupTaskListener()
        return setupInstanceControlListener()
    }

    private suspend fun setupDatabases() {
        modelsDatabase.connectToDatabase()
        instanceLogService.connectToDatabase()
        activityLogService.connectToDatabase()
    }
    private fun setupTaskListener() {
        GlobalScope.launch(Dispatchers.IO) {
            taskReceiveMessage
                .onEach { launch(taskExecutor) { handleTaskReceiveMessage(it) } }
                .collect()
        }
    }

    private fun setupInstanceControlListener(): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            instanceControlMessages
                .map { it }
                .flowOn(instanceExecutor)
                .onEach { handleInstanceMessage(it) }
                .collect()
        }
    }

    fun CoroutineScope.handleInstanceMessage(it: InstanceMessage) {
        launch(instanceExecutor) { MessageHandlerFactory.fromMessage(it).handle() }
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
                    val previousElems =
                        element.incoming
                            ?.map(process::sequenceFlowForId)
                            ?.map { process.elementForId(it.sourceRef) }
                            ?.toMutableList()
                            ?: mutableListOf()

                    val states =
                        previousElems.map {
                            activityLogService.getEvent(
                                it.id,
                                threadId,
                                eventState = EventState.FINISH
                            )
                        }

                    val canRun = !states.contains(null)

                    println("Can run ${element.id} : $canRun at ${Instant.now().toEpochMilli()}")

                    if (canRun) {
                        val elems =
                            element.outgoing
                                ?.map(process::sequenceFlowForId)
                                ?.map { process.elementForId(it.targetRef) }
                                ?.toMutableList()
                                ?: mutableListOf()
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

    suspend fun <T : DirectedElement> getElement(taskId: String): T {
        val log = activityLogService.getEvent(taskId, )!!
return  getElementFromLogItem(log)

    }
    suspend fun <T : DirectedElement> getElementFromLogItem(log: LoggedEventDocument): T {
        val instance = instanceLogService.getInstance(log.instanceId)

        val model = modelsDatabase.getModelById(instance.modelId)

        val process = model.getProcessById(instance.processId)

        val currentTask: DirectedElement = process.elementForId(log.elementId)
        return currentTask as T
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
                val threadVariables = instanceLogService.getVariables(it.threadId, it.instanceId)

                var mapped = it.taskVariables
                if (currentTask is Task) {
                    mapped = mapped.mapVariables(currentTask.extensionElements?.ioMapping?.output)
                }

                vars = threadVariables + mapped

                instanceLogService.saveVariables(
                    variables = vars,
                    threadId = it.threadId,
                    instanceId = it.instanceId
                )
            }
        }

        var semaphore = semaphoreMap.putIfAbsent(it.instanceId, Semaphore(1))
        if (semaphore == null) {
            semaphore = semaphoreMap[it.instanceId]!!
        }
        semaphore.acquire()
        // Save event to db
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
            continueWorkflowFromCurrentTask(currentTask, process, it, vars!!)
        }
        semaphore.release()
    }

    private suspend fun continueWorkflowFromCurrentTask(
        currentTask: DirectedElement,
        process: BpmnProcess,
        it: TaskReceiveMessage,
        variables: Variables
    ) {

        if (currentTask.outgoing != null) {
            val nextElements =
                currentTask.outgoing!!
                    .map {
                        val x = process.sequenceFlowForId(it)
                        process.elementForId(x.targetRef)
                    }
                    .toMutableList()

            val allRunnableElements: MutableList<Runnable> =
                generateTasks(
                    elements = nextElements,
                    instanceId = it.instanceId,
                    threadId = it.threadId,
                    vars = variables,
                    process = process
                )

            for (el in allRunnableElements) {
                GlobalScope.launch(taskExecutor) {
                    when (el.runnable) {
                        is ServiceTask -> {
                            taskMessages.emit(
                                ServiceTaskMessage(
                                    sendVariables =
                                        variables.mapVariables(
                                            (el.runnable as ServiceTask)
                                                ?.extensionElements
                                                ?.ioMapping
                                                ?.input
                                        ),
                                    instanceId = it.instanceId,
                                    taskId = UUID.randomUUID().toString().replace("-", ""),
                                    threadId = el.threadId,
                                    elementId = el.runnable.id,
                                    method =
                                        (el.runnable as ServiceTask).extensionElements!!.method!!,
                                    url = (el.runnable as ServiceTask).extensionElements!!.url!!
                                )
                            )
                        }
                        is UserTask -> {
                            taskMessages.emit(
                                UserFromMessage(
                                    sendVariables = mapOf(),
                                    instanceId = it.instanceId,
                                    taskId = UUID.randomUUID().toString().replace("-", ""),
                                    threadId = el.threadId,
                                    elementId = el.runnable.id
                                )
                            )
                        }
                        is StartEvent -> {
                            taskMessages.emit(
                                StartEventMessage(
                                    sendVariables = variables,
                                    instanceId = it.instanceId,
                                    taskId = UUID.randomUUID().toString().replace("-", ""),
                                    threadId = el.threadId,
                                    elementId = el.runnable.id
                                )
                            )
                        }
                    }

                    println("Running ${el.runnable.name}")
                    if (el.runnable.name == "end")
                        println("Workflow done ${Instant.now().toEpochMilli()}")
                }
            }
        }

        if (currentTask is EndEvent) {

            println("Workflow done ${Instant.now().toEpochMilli()}")
        }
    }

    fun Variables.mapVariables(mapping: MutableList<Mapping>?): Variables {
        val newMapping = mutableMapOf<String, Any?>()

        if (mapping != null) {
            for (m in mapping) {
                newMapping[m.target] = this[m.source]
            }
        }

        return newMapping
    }

    suspend fun getRunningTasks(): List<LoggedEventDocument> {
        return activityLogService.getEvents(EventState.RUNNING)
    }
}
