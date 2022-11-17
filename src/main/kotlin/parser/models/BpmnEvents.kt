

abstract class Event(
    id: String,
    name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    DirectedElement(id, name, outgoing, incoming)


data class StartEvent(
    override val id: String, override val name: String,
    override val outgoing: MutableList<String>,
    override val incoming: MutableList<String>?
) :
    Event(id, name, outgoing, incoming)


data class EndEvent(
    override val id: String, override val name: String,
    override val outgoing: MutableList<String>,
    override val incoming: MutableList<String>?
) :
    Event(id, name, outgoing, incoming)