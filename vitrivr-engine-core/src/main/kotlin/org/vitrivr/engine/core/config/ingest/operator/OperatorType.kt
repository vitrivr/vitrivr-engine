package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.general.Processor
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.ingest.*

/**
 * Ingestion [OperatorConfig] type names for serialisation purposes.
 *
 * Operators for ingestion are defined in the [org.vitrivr.engine.core.operators.ingest] package.
 */
@Serializable
enum class OperatorType {
    /**
     * [OperatorType] for a [OperatorConfig] describing an [Enumerator]
     */
    ENUMERATOR,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Decoder]
     */
    DECODER,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Transformer]
     */
    TRANSFORMER,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Processor]
     */
    PROCESSOR,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Extractor]
     */
    EXTRACTOR,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Exporter]
     */
    EXPORTER,

    /**
     * [OperatorType] for a [OperatorConfig] describing a [Retriever]
     */
    RETRIEVER
}
