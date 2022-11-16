package engine.parser.models

abstract class Gateway(
    id: String,
    name: String?,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    DirectedElement(id, name, outgoing, incoming)

data class ExclusiveGateway(
    override val id: String,
    override val name: String?,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    val default: String?,
) :
    Gateway(id, name, outgoing, incoming)


data class ParallelGateway(
    override val id: String,
    override val name: String?,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
) :
    Gateway(id, name, outgoing, incoming)