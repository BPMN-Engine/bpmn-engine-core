package flows

import bpmn.DirectedElement


abstract class ProcessFlow(
    open val id: String,
    open val source: DirectedElement,
    open val target: DirectedElement,
)


data class SeqFlow(
    override val id: String,
    override val source: DirectedElement,
    override val target: DirectedElement
) :
    ProcessFlow(id, source, target)









