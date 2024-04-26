package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.*

/**
 * Ingestion [OperatorConfig] type names for serialisation purposes.
 * Operators for ingestion are defined in the [org.vitrivr.engine.core.operators.ingest] package.
 */
@Serializable
enum class OperatorType {
    /**
     * [OperatorType] for a [OperatorConfig] describing an [Enumerator]
     */
    @SerialName("ENUMERATOR")
    ENUMERATOR,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Decoder]
     */
    @SerialName("DECODER")
    DECODER,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Transformer]
     */
    @SerialName("TRANSFORMER")
    TRANSFORMER,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Extractor]
     */
    @SerialName("EXTRACTOR")
    EXTRACTOR,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Exporter]
     */
    @SerialName("EXPORTER")
    EXPORTER,

    /**
     * [OperatorType] for a [OperatorConfig] describing an [Operator]
     */
    @SerialName("OPERATOR")
    OPERATOR,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Retriever]
     */
    @SerialName("RETRIEVER")
    RETRIEVER
}
