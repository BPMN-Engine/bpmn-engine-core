package engine.process_manager.runners

import engine.parser.models.ServiceTask
import engine.parser.models.Task
import engine.parser.models.UserTask
import engine.process_manager.context.WorkflowContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import message.Message
import message.UserFormMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


object TaskRunnerFactory {
    fun createFromTask(task: Task): TaskRunner {
        return when (task) {
            is ServiceTask -> ServiceTaskRunner(task = task)
            is UserTask -> UserTaskRunner(task = task)
            else -> UnimplementedTaskRunner(task = task)
//            else -> throw NotImplementedError("Not implemented for $task")
        }
    }

    infix fun fromTask(task: Task): TaskRunner {
        return createFromTask(task)
    }
}


interface TaskRunner : KoinComponent {
    val task: Task
    suspend fun run(workflowContext: WorkflowContext): Boolean

}


class UnimplementedTaskRunner(override val task: Task) : TaskRunner {

    override suspend fun run(workflowContext: WorkflowContext): Boolean {

        return true
    }


}


class ServiceTaskRunner(override val task: ServiceTask) : TaskRunner {

    override suspend fun run(workflowContext: WorkflowContext): Boolean {


        val d = task.name.toString().replace("task", "").toLong()
        delay(d * 1000)
        return true

    }


}


abstract class MessageTaskRunner(

) : TaskRunner {
    inline fun <reified T : Message> channel(): Flow<T> {
        val channel: MutableSharedFlow<Message> by inject()

        return channel.filterIsInstance()
    }
}


class UserTaskRunner(override val task: UserTask) :
    MessageTaskRunner() {


    override suspend fun run(workflowContext: WorkflowContext): Boolean {

        val x = runBlocking {
            channel<UserFormMessage>().first(::predicate)
        }

//        workflowContext.updateState(x.form)

        return true


    }

    private fun predicate(it: Message): Boolean {
        if (it is UserFormMessage) {
            return it.form.containsKey("test")
        }
        return false
    }


}
