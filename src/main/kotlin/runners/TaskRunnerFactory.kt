package engine.process_manager.runners

import bpmn.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import engine.messaging.receive_message.TaskCompleteMessage
import engine.messaging.receive_message.TaskFailedMessage
import engine.messaging.receive_message.TaskReceiveMessage
import engine.messaging.task_message_service.messages.ServiceTaskMessage
import engine.messaging.task_message_service.messages.StartEventMessage
import engine.messaging.task_message_service.messages.TaskSendMessage
import engine.process_manager.models.Variables
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

val client = HttpClient.newHttpClient()

class ServiceTaskRunner(override val task: ServiceTaskMessage) : TaskRunner {

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun run(): Variables {

        val requestBody: String = jsonMapper.writeValueAsString(task.sendVariables)

        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(task.url))
                .method(task.method, BodyPublishers.ofString(requestBody))
                .build()

        val response =
            GlobalScope.async(Dispatchers.IO) {
                    client.send(request, HttpResponse.BodyHandlers.ofString())
                }
                .await()

        return jsonMapper.readValue(response.body())
    }
}

class StartEventTaskRunner(override val task: StartEventMessage) : TaskRunner {

    override suspend fun run(): Variables {
        return task.sendVariables
    }
}
