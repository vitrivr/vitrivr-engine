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