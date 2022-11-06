package bpmn

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode

class ProcessDeserializer : JsonDeserializer<BpmnProcess>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext, intoValue: BpmnProcess): BpmnProcess {
        val codec: ObjectCodec = p.codec
        val json: JsonNode = codec.readTree(p)
        val `object`: ObjectNode = json as ObjectNode

        return codec.treeToValue(json, BpmnProcess::class.java)
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BpmnProcess {
        val codec: ObjectCodec = p.codec
        val json: JsonNode = codec.readTree(p)


        return codec.treeToValue(json, BpmnProcess::class.java)
    }
}

class TasksDeserializer : StdDeserializer<MutableList<Task>>(mutableListOf<Task>()::class.java) {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
        intoValue: MutableList<Task>
    ): MutableList<Task> {
        println(ctxt)


        TODO("Not yet implemented")
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): MutableList<Task> {
        println(ctxt)
        TODO("Not yet implemented")
    }
}