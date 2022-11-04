import bpmn.BpmnModel
import bpmn.BpmnProcess
import bpmn.StartEvent
import java.util.UUID

class BpmnInstanceRunner {
    suspend fun createInstance(model: BpmnModel) {
        val process: BpmnProcess = model.process[0];

        val _id: String = UUID.randomUUID().toString();
        val startEvent: StartEvent = process.startEvent.first()


    }
}