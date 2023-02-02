import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import engine.parser.models.*

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
