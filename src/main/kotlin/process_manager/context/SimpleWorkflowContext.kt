package engine.process_manager.context

import engine.parser.models.Mapping

class SimpleWorkflowContext(override val instanceId: String) : WorkflowContext {
    override var contextVariables: Map<String, Any?> = mutableMapOf()

    override suspend fun updateState(values: Map<String, Any?>) {
        contextVariables = contextVariables.plus(values)
    }

    override suspend fun updateState(values: Map<String, Any?>, outputMapping: MutableList<Mapping>) {
        val data = mutableMapOf<String, Any?>()

        for (mapping in outputMapping) {
            data[mapping.target] = values[mapping.source.replace("=", "")]
        }
        return updateState(data)

    }

    override suspend fun dataFromInput(input: MutableList<Mapping>): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>()
        for (mapping in input) {
            data[mapping.target] = contextVariables[mapping.source.replace("=", "")]
        }
        return data
    }

    override fun toString(): String {
        return "SimpleWorkflowState(state=$contextVariables)"
    }


}