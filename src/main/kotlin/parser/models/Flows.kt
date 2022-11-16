package engine.parser.models

import com.fasterxml.jackson.annotation.JsonProperty

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

