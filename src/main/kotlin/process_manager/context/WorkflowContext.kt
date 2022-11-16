package engine.process_manager.context

import engine.parser.models.Mapping

interface WorkflowContext {
    val instanceId: String
    val contextVariables: Map<String, Any?>

    suspend fun updateState(values: Map<String, Any?>)
    suspend fun updateState(values: Map<String, Any?>, outputMapping: MutableList<Mapping>)
    suspend fun dataFromInput(input: MutableList<Mapping>): Map<String, Any?>;


}