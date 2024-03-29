package engine.parser.models

import BasicExtensionElements
import ExtensionElements
import RunnableDirectedElement
import bpmn.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue

abstract class Task(
    id: String,
    name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    open val extensionElements: ExtensionElements?,
) : RunnableDirectedElement(id, name, outgoing, incoming)

data class ManualTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?
) : Task(id, name, outgoing, incoming, extensionElements)

data class UserTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?
) : Task(id, name, outgoing, incoming, extensionElements) {
    val userTaskForm: Map<String, Any>? by lazy {
       jsonMapper.readValue(extensionElements?.getHeaderValue("form")?:"{}" )
    }
}

data class ServiceTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?
) : Task(id, name, outgoing, incoming, extensionElements)

data class SendTask(
    override val id: String,
    override val name: String,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    override val extensionElements: BasicExtensionElements?
) : Task(id, name, outgoing, incoming, extensionElements)
