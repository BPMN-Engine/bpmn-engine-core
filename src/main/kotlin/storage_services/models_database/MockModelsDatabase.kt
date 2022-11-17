package engine.database_connector.models_database

import BpmnModel

class MockModelsDatabase : ModelsDatabase {

    val models = mutableListOf<BpmnModel>()


    override suspend fun getModels(): List<BpmnModel> {
        return models;
    }

    override suspend fun saveModel(model: BpmnModel): BpmnModel {
        models.add(model);
        return model
    }

    override suspend fun connectToDatabase() {

    }
}