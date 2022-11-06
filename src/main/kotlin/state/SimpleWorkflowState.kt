package state

import bpmn.Mapping

class SimpleWorkflowState(override val instanceId: String) : WorkflowState {
    var state: Map<String, Any?> = mutableMapOf()

    override suspend fun updateState(values: Map<String, Any?>) {
        state = state.plus(values)
    }

    override suspend fun updateState(values: Map<String, Any?>, outputMapping: MutableList<Mapping>) {
        val data = mutableMapOf<String, Any?>()

        for (mapping in outputMapping) {
            data[mapping.target] = values[mapping.source.replace("=", "")]
        }
        return updateState(data)

    }

    override fun dataFromInput(input: MutableList<Mapping>): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>()
        for (mapping in input) {
            data[mapping.target] = state[mapping.source.replace("=", "")]
        }
        return data
    }

    override fun toString(): String {
        return "SimpleWorkflowState(state=$state)"
    }


}