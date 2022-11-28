package engine.database_connector.models_database

import BpmnModel
import bpmn.BpmnParser
import engine.storage_services.models_database.ModelsDatabase

class MockModelsDatabase : ModelsDatabase {


    val models = mutableListOf<BpmnModel>()


    override suspend fun getModels(): List<BpmnModel> {
        return models;
    }

    override suspend fun saveModel(model: BpmnModel): BpmnModel {
        models.add(model);
        return model
    }

    override suspend fun getModelById(modelId: String, versionId: String?): BpmnModel {
        return models.first()
    }

    override suspend fun connectToDatabase() {
        saveModel(BpmnParser.parse())
    }
}