package bpmn

import com.fasterxml.jackson.annotation.*


data class BpmnModel(

    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val process: MutableList<BpmnProcess>,
    val id: String
)


open class BpmnObject(
    open val id: String,
    open val name: String,
)

open class DirectedObject(
    id: String,
    name: String,
    open val incoming: MutableList<String>?,
    open val outgoing: MutableList<String>?,

    ) : BpmnObject(id, name)


abstract class Variable(
    open val rawValue: String,
)

class ExpressionVariable(rawValue: String) : Variable(rawValue) {
}


@JsonRootName("process")
data class BpmnProcess constructor(

    override val id: String, override val name: String,
    @JsonProperty("userTask")
    private val userTask: MutableList<UserTask>,
    private val manualTask: MutableList<ManualTask>,
    private val serviceTask: MutableList<ServiceTask>,
    private val sequenceFlow: MutableList<SequenceFlow>,
    private val sendTask: MutableList<SendTask>,
    val startEvent: MutableList<StartEvent>


) :
    BpmnObject(id, name) {
    val tasks: MutableList<Task> = (userTask + manualTask + serviceTask + sendTask).toMutableList()
    val events: MutableList<Event> = (startEvent).toMutableList()
}


data class SequenceFlow(
    val id: String,
    val sourceRef: String,
    val targetRef: String
)

