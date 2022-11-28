package engine.storage_services.models_database

import BpmnModel
import engine.storage_services.DatabaseConnector


interface ModelsDatabase : DatabaseConnector {
    suspend fun getModels(): List<BpmnModel>
    suspend fun saveModel(model: BpmnModel): BpmnModel
    suspend fun getModelById(modelId: String, versionId: String?=null): BpmnModel
}