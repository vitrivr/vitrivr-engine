package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object InputDataSerializer : JsonContentPolymorphicSerializer<InputData>(InputData::class) {

    private val serializerMap = mutableMapOf(
        "TEXT" to TextInputData.serializer(),
        "IMAGE" to ImageInputData.serializer(),
        "VECTOR" to FloatVectorInputData.serializer(),
        "ID" to RetrievableIdInputData.serializer(),
        "BOOLEAN" to BooleanInputData.serializer(),
        "NUMERIC" to NumericInputData.serializer(),
        "DATE" to DateInputData.serializer(),
        "LIST" to ListInputData.serializer(),
        "STRUCT" to StructInputData.serializer()
    )

    init {
        //TODO load additional types if available
    }

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<InputData> {

        val typeName = element.jsonObject["type"]?.jsonPrimitive?.content?.uppercase()
            ?: throw IllegalArgumentException("type not specified")

        return serializerMap[typeName.uppercase()] ?: throw IllegalArgumentException("Unknown InputData type '$typeName'")
    }
}
