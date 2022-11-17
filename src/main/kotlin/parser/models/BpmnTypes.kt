
import com.fasterxml.jackson.annotation.*


data class BpmnModel(
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val process: MutableList<BpmnProcess>,
    val id: String
)


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


abstract class ExtensionElements(
    open val ioMapping: IoMapping?,
)

data class BasicExtensionElements(override val ioMapping: IoMapping?) : ExtensionElements(ioMapping)

data class IoMapping(
    val input: MutableList<Mapping>?,
    val output: MutableList<Mapping>?
)

data class Mapping(
    val source: String,
    val target: String,
)




