package bpmn


abstract class Task(
    id: String,
    name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    open val extensionElements: ExtensionElements?,

    ) :
    DirectedElement(id, name, outgoing, incoming)

data class ManualTask(
    override val id: String, override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?
) :
    Task(id, name, outgoing, incoming, extensionElements)

data class UserTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?, override val extensionElements: BasicExtensionElements?


) :
    Task(id, name, outgoing, incoming, extensionElements)

data class ServiceTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?

) :
    Task(id, name, outgoing, incoming, extensionElements)


data class SendTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?


) :
    Task(id, name, outgoing, incoming, extensionElements)

