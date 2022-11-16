package engine.database_connector.models_database

import engine.database_connector.DatabaseConnector
import engine.parser.models.BpmnModel


interface ModelsDatabase : DatabaseConnector {
      suspend fun getModels():List<BpmnModel>
      suspend fun saveModel(model: BpmnModel):BpmnModel
}