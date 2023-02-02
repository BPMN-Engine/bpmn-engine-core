package bpmn

import BpmnModel
import com.ctc.wstx.stax.WstxInputFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.nio.file.Paths
import javax.xml.stream.XMLInputFactory
val jsonMapper = ObjectMapper()
object BpmnParser {
    fun parse(): BpmnModel {
        val module = JacksonXmlModule()

        val input: XMLInputFactory = WstxInputFactory()
        input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)

        module.setDefaultUseWrapper(false)
        val xmlMapper = XmlMapper(module).registerKotlinModule()
        val path = Paths.get("").toAbsolutePath().toString()

        val file = File("resources/test.bpmn")
        val node: JsonNode = xmlMapper.readTree(file.readBytes())


        jsonMapper .configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        ).configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)

        val kotlinModule = KotlinModule.Builder()
            .configure(KotlinFeature.NullToEmptyCollection, true)
            .build()



        jsonMapper.registerModule(kotlinModule)
        val json: String = jsonMapper.writeValueAsString(node)

        File("resources/output.json").writeText(json)

        val model = jsonMapper.readValue(
            json,
            BpmnModel::class.java
        )

        return model
    }
}