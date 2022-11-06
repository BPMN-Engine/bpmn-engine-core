package bpmn

import com.fasterxml.jackson.annotation.*


data class BpmnModel(

    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val process: MutableList<BpmnProcess>,
    val id: String
)


abstract class BpmnElement(
    open val id: String,
    open val name: String?,
)

abstract class DirectedElement(
    id: String,
    name: String?,
    open val incoming: MutableList<String>?,
    open val outgoing: MutableList<String>?,

    ) : BpmnElement(id, name = name ?: id)


abstract class ExtensionElements(
    open val ioMapping: IoMapping?,
)

data class BasicExtensionElements(override val ioMapping: IoMapping?) : ExtensionElements(ioMapping)

data class IoMapping(
    val input: MutableList<Mapping>?,
    val output: MutableList<Mapping>?
)

data class Mapping(
    val source: String,
    val target: String,
)


@JsonRootName("process")
data class BpmnProcess constructor(

    override val id: String, override val name: String,
    @JsonProperty("userTask")
    private val userTask: MutableList<UserTask>,
    private val manualTask: MutableList<ManualTask>,
    private val serviceTask: MutableList<ServiceTask>,
    val sequenceFlow: MutableList<SequenceFlow>,
    private val sendTask: MutableList<SendTask>,
    private val exclusiveGateway: MutableList<ExclusiveGateway>,
    private val parallelGateway: MutableList<ParallelGateway>,

    val startEvent: MutableList<StartEvent>,
    val endEvent: MutableList<EndEvent>


) :
    BpmnElement(id, name) {
    fun sequenceFlowForId(id: String): SequenceFlow {
        return sequenceFlow.find { it.id == id }!!
    }

    fun elementForId(id: String): DirectedElement {
        return steps.find { it.id == id }!!
    }

    val tasks: MutableList<Task> = (userTask + manualTask + serviceTask + sendTask).toMutableList()
    val events: MutableList<Event> = (startEvent + endEvent).toMutableList()
    val steps: MutableList<DirectedElement> = (tasks + events + exclusiveGateway + parallelGateway).toMutableList()
}


data class SequenceFlow(
    val id: String,
    val sourceRef: String,
    val targetRef: String,
    @JsonProperty("conditionExpression")
    private val expressionMap: Map<String, String>?,
) {

    val expression = createConditionExpr()


    private fun createConditionExpr(): Map<String, String>? {
        if (expressionMap == null) return null
        val newMap = expressionMap.toMutableMap()
        val value = newMap.remove("")
        newMap["condition"] = value.toString()
        return newMap
    }

    override fun toString(): String {
        return "SequenceFlow(id='$id', sourceRef='$sourceRef', targetRef='$targetRef', expression=$expression)"
    }
}

