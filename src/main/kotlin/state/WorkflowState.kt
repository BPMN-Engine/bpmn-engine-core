package state

import bpmn.Mapping

interface WorkflowState {
    val instanceId: String
    suspend fun updateState(values: Map<String, Any?>)
    suspend fun updateState(values: Map<String, Any?>,outputMapping: MutableList<Mapping>)
    fun dataFromInput(input: MutableList<Mapping>): Map<String, Any?>;


}