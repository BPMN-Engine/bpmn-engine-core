package runners

import bpmn.ServiceTask
import bpmn.Task
import bpmn.UserTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import message.Message
import message.UserFormMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import state.WorkflowState


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
    suspend fun run(workflowState: WorkflowState): Boolean

}


class UnimplementedTaskRunner(override val task: Task) : TaskRunner {

    override suspend fun run(workflowState: WorkflowState): Boolean {

        return true
    }


}


class ServiceTaskRunner(override val task: ServiceTask) : TaskRunner {

    override suspend fun run(workflowState: WorkflowState): Boolean {
        val requestData: Map<String, Any?> = workflowState.dataFromInput(task.extensionElements!!.ioMapping!!.input!!)
        println(requestData)

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


    override suspend fun run(workflowState: WorkflowState): Boolean {

        val x = runBlocking {
            channel<UserFormMessage>().first(::predicate)
        }

        workflowState.updateState(x.form)

        return true


    }

    private fun predicate(it: Message): Boolean {
        if (it is UserFormMessage) {
            return it.form.containsKey("test")
        }
        return false
    }


}
