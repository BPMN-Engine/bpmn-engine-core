package runners

import bpmn.ServiceTask
import bpmn.Task
import bpmn.UserTask


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

fun main() {
    val factory: TaskRunnerFactory = TaskRunnerFactory

    val x =
        factory.createFromTask(
            ServiceTask(
                id = "",
                name = "",
                inputs = mutableListOf(),
                outputs = mutableListOf(),
                outgoing = null,
                incoming = null
            )
        )

}


interface TaskRunner {
    val task: Task
    suspend fun run()

}


class UnimplementedTaskRunner(override val task: Task) : TaskRunner {

    override suspend fun run() {
    }


}


class ServiceTaskRunner(override val task: ServiceTask) : TaskRunner {

    override suspend fun run() {
    }


}

class UserTaskRunner(override val task: UserTask) : TaskRunner {

    override suspend fun run() {

    }

}
