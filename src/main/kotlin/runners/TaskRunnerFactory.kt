package engine.process_manager.runners

import bpmn.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import engine.messaging.receive_message.TaskCompleteMessage
import engine.messaging.receive_message.TaskFailedMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.task_message_service.messages.ServiceTaskMessage
import engine.messaging.task_message_service.messages.StartEventMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.messaging.task_message_service.messages.UserFromMessage
import engine.process_manager.models.Variables
import engine.process_manager.taskExecutor
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import kotlinx.coroutines.*

object TaskRunnerFactory {

    suspend fun createFromMessage(message: TaskSendMessage): TaskReceiveMessage {
        val runner =
            when (message) {
                is ServiceTaskMessage -> ServiceTaskRunner(task = message)
                is StartEventMessage -> StartEventTaskRunner(task = message)
                is UserFromMessage -> UserFromRunner(task = message)
                else -> UnimplementedTaskRunner(task = message)
            //            else -> throw NotImplementedError("Not implemented for $task")
            }

        try {
            val x = runBlocking { runner.run() }
            return TaskCompleteMessage(
                instanceId = message.instanceId,
                taskId = message.taskId,
                taskVariables = x,
                threadId = message.threadId,
                elementId = message.elementId
            )
        } catch (e: Exception) {
            return TaskFailedMessage(
                instanceId = message.instanceId,
                taskId = message.taskId,
                threadId = message.threadId,
                error = e.message.toString(),
                elementId = message.elementId
            )
        }
    }

    fun isMessageHandledInternally(message: TaskSendMessage): Boolean {
        return message !is UserFromMessage
    }
}

interface TaskRunner {
    val task: TaskSendMessage
    suspend fun run(): Variables
}

class UnimplementedTaskRunner(override val task: TaskSendMessage) : TaskRunner {

    override suspend fun run(): Variables {
        return emptyMap()
    }
}

val client = HttpClient.newBuilder().build()

class ServiceTaskRunner(override val task: ServiceTaskMessage) : TaskRunner {

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun run(): Variables {

        val requestBody: String = jsonMapper.writeValueAsString(task.sendVariables)
        println("body $requestBody")
        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(task.url))
                .method(task.method.toUpperCase(), BodyPublishers.ofString(requestBody))
                .build()

        val response =
            GlobalScope.async(taskExecutor) {
                    client.send(request, HttpResponse.BodyHandlers.ofString())
                }
                .await()

        try {
            return jsonMapper.readValue(response.body())
        } catch (e: Exception) {
            println("task id ${task.elementId}")
            println("request url ${request.uri()}")
            println("request body ${request.bodyPublisher()}")
            throw e
        }
    }
}

class StartEventTaskRunner(override val task: StartEventMessage) : TaskRunner {

    override suspend fun run(): Variables {
        return task.sendVariables
    }
}

class UserFromRunner(override val task: UserFromMessage) : TaskRunner {

    override suspend fun run(): Variables {
        return task.sendVariables
    }
}
