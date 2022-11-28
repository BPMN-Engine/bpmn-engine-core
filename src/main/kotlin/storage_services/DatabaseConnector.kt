package engine.storage_services

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential

interface DatabaseConnector {
    suspend fun connectToDatabase()
}


