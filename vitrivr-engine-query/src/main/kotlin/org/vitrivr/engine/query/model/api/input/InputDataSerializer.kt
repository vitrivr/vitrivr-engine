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
            InputType.BOOLEANVECTOR -> BooleanVectorInputData.serializer()
            InputType.DOUBLEVECTOR -> DoubleVectorInputData.serializer()
            InputType.FLOATVECTOR -> FloatVectorInputData.serializer()
            InputType.INTVECTOR -> IntVectorInputData.serializer()
            InputType.LONGVECTOR -> LongVectorInputData.serializer()
            InputType.BOOLEAN -> BooleanInputData.serializer()
            InputType.BYTE -> ByteInputData.serializer()
            InputType.DOUBLE -> DoubleInputData.serializer()
            InputType.FLOAT -> FloatInputData.serializer()
            InputType.INT -> IntInputData.serializer()
            InputType.LONG -> LongInputData.serializer()
            InputType.SHORT -> ShortInputData.serializer()
            InputType.STRING -> StringInputData.serializer()
            InputType.ID -> RetrievableIdInputData.serializer()
            InputType.DATE -> DateInputData.serializer()
        }
    }
}
