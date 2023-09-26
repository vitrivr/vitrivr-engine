package org.vitrivr.engine.query.model.api.operator

import kotlinx.serialization.Serializable

@Serializable(with = OperationDescriptionSerializer::class)
sealed class OperatorDescription{
    abstract val type: OperatorType
}

@Serializable
data class RetrieverDescription(val input: String) : OperatorDescription() {
    override val type = OperatorType.RETRIEVER
}

@Serializable
data class TransformerDescription(
    val transformerName: String,
    val input: String,
    val properties: Map<String, String> = emptyMap()
) : OperatorDescription() {
    override val type = OperatorType.TRANSFORMER
}

@Serializable
data class AggregatorDescription(
    val aggregatorName: String,
    val inputs: List<String>,
    val properties: Map<String, String> = emptyMap()
) : OperatorDescription() {
    override val type = OperatorType.AGGREGATOR
}