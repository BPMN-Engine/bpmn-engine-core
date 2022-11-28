package engine.process_manager.tasks

import RunnableDirectedElement
 import engine.parser.models.Event
import engine.parser.models.Task


abstract class Runnable(open val threadId:String,open val runnable: RunnableDirectedElement)

data class RunnableTask(override val threadId:String,override val runnable: Task) : Runnable(threadId,runnable)
data class RunnableEvent(override val threadId:String, override val runnable: Event) : Runnable(threadId, runnable)