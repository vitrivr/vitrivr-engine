package org.vitrivr.engine.query.model.api.operator
import kotlinx.serialization.Serializable

@Serializable(with = OperationDescriptionSerializer::class)
sealed class OperatorDescription{
    abstract val type: OperatorType
}

@Serializable
data class RetrieverDescription(
    /** The name of the input in the information need */
    val input: String,

    /** The name of the field in the schema */
    val field: String? = null
) : OperatorDescription() {
    override val type = OperatorType.RETRIEVER
}

@Serializable
data class TransformerDescription(
    val transformerName: String,
    val input: String,
) : OperatorDescription() {
    override val type = OperatorType.TRANSFORMER
}

@Serializable
data class AggregatorDescription(
    val aggregatorName: String,
    val inputs: List<String>,
) : OperatorDescription() {
    override val type = OperatorType.AGGREGATOR
}

@Serializable
data class InputTransformerDescription(
    val transformerName: String,
    val inputs: List<String>
) : OperatorDescription() {
    override val type = OperatorType.INPUT_TRANSFORMER
}