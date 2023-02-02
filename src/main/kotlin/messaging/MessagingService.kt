package engine.messaging


interface MessagingService {
    suspend fun setup(): MessagingService
}