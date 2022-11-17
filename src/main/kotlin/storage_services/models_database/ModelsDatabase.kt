package engine.database_connector.models_database

import BpmnModel
import engine.database_connector.DatabaseConnector


interface ModelsDatabase : DatabaseConnector {
      suspend fun getModels():List<BpmnModel>
      suspend fun saveModel(model: BpmnModel): BpmnModel
}