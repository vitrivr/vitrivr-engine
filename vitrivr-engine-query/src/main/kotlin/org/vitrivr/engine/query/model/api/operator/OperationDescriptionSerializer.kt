package org.vitrivr.engine.query.model.api.operator

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object OperationDescriptionSerializer: JsonContentPolymorphicSerializer<OperatorDescription>(OperatorDescription::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OperatorDescription> {
        val typeName = element.jsonObject["type"]?.jsonPrimitive?.content?.uppercase() ?: throw IllegalArgumentException("type not specified")

        return when(OperatorType.valueOf(typeName)) {
            OperatorType.RETRIEVER -> RetrieverDescription.serializer()
            OperatorType.TRANSFORMER -> TransformerDescription.serializer()
            OperatorType.AGGREGATOR -> AggregatorDescription.serializer()
            OperatorType.BOOLEAN_AND -> BooleanAndDescription.serializer()
        }
    }
}
