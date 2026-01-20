package org.vitrivr.engine.query.model.api.operator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OperatorType {
    @SerialName("RETRIEVER")
    RETRIEVER,
    @SerialName("TRANSFORMER")
    TRANSFORMER,
    @SerialName("AGGREGATOR")
    AGGREGATOR,
    @SerialName("BOOLEAN_AND")
    BOOLEAN_AND
}
