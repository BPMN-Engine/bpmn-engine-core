package com.leo.service

 import com.rabbitmq.client.*
 import engine.messaging.message.StartInstanceMessage
 import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named




class RabbitSubscriptionService : KoinComponent {
    private val instanceInitiator: MutableSharedFlow<StartInstanceMessage> by inject( )

    private val connectionFactory: ConnectionFactory = ConnectionFactory()

    private lateinit var connection: Connection
    private lateinit var channel: Channel


    companion object {

        const val EXCHANGE = "modelexchange"
        const val QUEUE = "q1"


        //replace this later
        const val INSTANCE_NAME = "instance#1"

    }


    init {
        connectionFactory.host = "localhost"
        connectionFactory.port = 5672
//        connectionFactory.virtualHost = "/"
        connectionFactory.username = "guest"
        connectionFactory.password = "guest"
//        connectionFactory.isAutomaticRecoveryEnabled = true


//
//            ClientParameters()
//                .url("http://127.0.0.1:15672/api/")
//                .username("guest")
//                .password("guest")

    }

    fun getChannel(): Channel {
        if (!channel.isOpen()) {
            channel = connection.createChannel()
        }
        return channel
    }

    fun setupQueue(): RabbitSubscriptionService {
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()

        try {
            getChannel().queueDeclarePassive(QUEUE)

        } catch (e: Exception) {
            println(e)
            getChannel().queueDeclare(QUEUE, true, false, false, emptyMap())


        }
        try {
            getChannel().queueBind(QUEUE, EXCHANGE, INSTANCE_NAME)

        } catch (e:Exception) {
            println(e)

        }

        return this
    }

    fun startListening() {

        println("Listening")
        val c =              connection.createChannel()

        val deliverCallback = DeliverCallback { consumerTag: String, delivery: Delivery ->
            println("delivered")

            runBlocking {

                c.basicAck(delivery.envelope.deliveryTag, false)
            }
        }


        val cancelCallback = CancelCallback { consumerTag: String? -> println("Cancelled... $consumerTag") }

        c.basicConsume(
            QUEUE,
            false,
            deliverCallback,
            cancelCallback
        )

    }


}