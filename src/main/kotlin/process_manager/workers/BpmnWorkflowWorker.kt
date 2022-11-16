package engine.process_manager.workers

import bpmn.*
import engine.process_manager.flows.ProcessFlow
import engine.process_manager.flows.SeqFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import message.Message
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import engine.process_manager.runners.TaskRunnerFactory
import engine.process_manager.context.SimpleWorkflowContext
import engine.parser.models.*
import java.util.*

@OptIn(DelicateCoroutinesApi::class)

class BpmnWorkflowWorker : KoinComponent {


    private val messageChannel: MutableSharedFlow<Message> by inject()

    suspend fun createInstance(model: BpmnModel, startEvent: StartEvent? = null) {


        val process: BpmnProcess = model.process[0]
        val _id: String = UUID.randomUUID().toString()
        val state = SimpleWorkflowContext(_id)

        val initialEvent = startEvent ?: process.startEvent.first()


        val toAdd = mutableListOf<ProcessFlow>()
        val flows = mutableListOf<ProcessFlow>()


        for (o in initialEvent.outgoing) {
            val sq = process.sequenceFlowForId(o)
            flows.add(element = SeqFlow(id = sq.id, source = initialEvent, target = process.elementForId(sq.targetRef)))
        }



        while (flows.isNotEmpty()) {
            val futures: MutableList<Deferred<Unit>> = mutableListOf()

            for (o in flows) {
                val target = o.target
                println("Running ${target.name}")

                for (c in target.outgoing!!) {
                    var sq = process.sequenceFlowForId(c)
                    var targetElement = process.elementForId(sq.targetRef)
                    if (targetElement is ExclusiveGateway) {
                        //test expression
                        var newFlow: SequenceFlow? = null
                        for (flow in targetElement.outgoing!!) {
                            val mapped = process.sequenceFlowForId(targetElement.default!!)
                            //if expresson ok
                            newFlow = mapped
                            break
                        }
                        //use default

                        sq = newFlow ?: process.sequenceFlowForId(targetElement.default!!)
                        targetElement = process.elementForId(sq.targetRef)

                    }
                    toAdd.add(
                        element = SeqFlow(
                            id = sq.id,
                            source = target,
                            target = targetElement
                        )
                    )
                }

                if (target is Task) {
                    val future = GlobalScope.async {
                        TaskRunnerFactory.fromTask(target).run(state)
                        println("Completed ${target.name}")

                    }
                    futures.add(future)
                }

                if (target is EndEvent) {
                    println("Completed flow")
                }


            }
            flows.clear()
            flows.addAll(toAdd.toMutableList())
            toAdd.clear()
            futures.awaitAll()
        }
        println("Finished with $state")
    }
}



