import com.fasterxml.jackson.annotation.JsonFormat

data class BpmnModel(
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val process: MutableList<BpmnProcess>,
    val id: String
) {
    fun getProcessById(id: String): BpmnProcess {
        return process.first { it.id == id }
    }
}

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

abstract class RunnableDirectedElement(
    id: String,
    name: String?,
    override val incoming: MutableList<String>?,
    override val outgoing: MutableList<String>?,
) : DirectedElement(id, name = name ?: id, incoming, outgoing)

abstract class ExtensionElements(
    open val ioMapping: IoMapping?,
)

data class BasicExtensionElements(
    override val ioMapping: IoMapping?,
    private val taskHeaders: Map<String, List<*>>?
) : ExtensionElements(ioMapping) {

    var url = url()
    var method = method()

    private fun getHeaderValue(key: String): String? {
        val header = taskHeaders?.get("header")

            val el = header?.find { (it as Map<*, *>)["key"] == key } as Map<*, *>?

            return el?.get("value")?.toString()
        }



    private fun url() = getHeaderValue("url")
    private fun method() = getHeaderValue("method")?:"GET"
}

data class IoMapping(val input: MutableList<Mapping>?, val output: MutableList<Mapping>?)

data class Mapping(
    val source: String,
    val target: String,
)
