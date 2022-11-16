package engine.database_connector

interface DatabaseConnector {
    suspend fun connectToDatabase()
}