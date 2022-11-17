

data class InputOutput(
    val inputParameter: MutableList<Map<String, String>>?,
    val outputParameter: MutableList<Map<String, String>>?
)

abstract class ExtensionElement(
    val inputOutput: InputOutput?
)
