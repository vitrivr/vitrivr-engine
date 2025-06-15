package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object InputDataSerializer : JsonContentPolymorphicSerializer<InputData>(InputData::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<InputData> {

        val typeName = element.jsonObject["type"]?.jsonPrimitive?.content?.uppercase() ?: throw IllegalArgumentException("type not specified")

        return when(InputType.valueOf(typeName)) {
            InputType.TEXT -> TextInputData.serializer()
            InputType.IMAGE -> ImageInputData.serializer()
            InputType.VECTOR -> VectorInputData.serializer()
            InputType.ID -> RetrievableIdInputData.serializer()
            InputType.BOOLEAN -> BooleanInputData.serializer()
            InputType.NUMERIC -> NumericInputData.serializer()
            InputType.DATETIME -> DateTimeInputData.serializer()
            InputType.GEOGRAPHY -> GeographyInputData.serializer()
        }
    }
}
