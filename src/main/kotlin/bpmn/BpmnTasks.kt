package bpmn


abstract class Task(
    id: String,
    name: String,
    open val inputs: MutableList<Variable>,
    open val outputs: MutableList<Variable>,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    DirectedObject(id, name, outgoing, incoming) {
}


data class ManualTask(
    override val id: String, override val name: String,
    override val inputs: MutableList<Variable>,
    override val outputs: MutableList<Variable>,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?
) :
    Task(id, name, inputs, outputs, outgoing, incoming) {

}

data class UserTask(
    override val id: String,
    override val name: String,
    override val inputs: MutableList<Variable>,
    override val outputs: MutableList<Variable>,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    Task(id, name, inputs, outputs, outgoing, incoming) {
}

data class ServiceTask(
    override val id: String,
    override val name: String,
    override val inputs: MutableList<Variable>,
    override val outputs: MutableList<Variable>,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    Task(id, name, inputs, outputs, outgoing, incoming) {
}


data class SendTask(
    override val id: String,
    override val name: String,
    override val inputs: MutableList<Variable>,
    override val outputs: MutableList<Variable>, override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    Task(id, name, inputs, outputs, outgoing, incoming) {
}

