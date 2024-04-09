package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.Aggregator

/**
 * Ingestion [OperatorConfig] type names for serialisation purposes.
 * Operators for ingestion are defined in the [org.vitrivr.engine.core.operators.ingest] package.
 */
@Serializable
enum class OperatorType {
    /**
     * [OperatorType] for a [OperatorConfig] for a [Decoder]
     */
    @SerialName("DECODER")
    DECODER,

    /**
     * [OperatorType] for a [OperatorConfig] for a [Transformer]
     */
    @SerialName("TRANSFORMER")
    TRANSFORMER,

    /**
     * [OperatorType] for a [OperatorConfig] for a [Extractor]
     */
    @SerialName("EXTRACTOR")
    EXTRACTOR,

    /**
     * [OperatorType] for a [OperatorConfig] for a [Exporter]
     */
    @SerialName("EXPORTER")
    EXPORTER,

    /**
     * [OperatorType] for a [OperatorConfig] for a [Aggregator]
     */
    @SerialName("AGGREGATOR")
    AGGREGATOR
}
