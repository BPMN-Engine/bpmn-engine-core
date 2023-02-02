package engine.storage_services

interface DatabaseConnector {
    suspend fun connectToDatabase()
}


