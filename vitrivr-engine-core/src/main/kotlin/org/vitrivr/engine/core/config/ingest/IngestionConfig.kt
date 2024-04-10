package org.vitrivr.engine.core.config.ingest

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ContextConfig
import org.vitrivr.engine.core.config.ingest.operation.OperationsConfig
import org.vitrivr.engine.core.config.ingest.operator.DecoderConfig
import org.vitrivr.engine.core.config.ingest.operator.EnumeratorConfig
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig

/**
 * Configuration of an ingestion pipeline for indexing.
 */
@Serializable
data class IngestionConfig(
    /**
     * The name of the schema for this [IngestionConfig].
     * Ultimately, ingestion is performed within said schema.
     */
    val schema: String,

    /**
     * The [ContextConfig] for this [IngestionConfig]'s context.
     */
    val context: ContextConfig,

    /**
     * The [EnumeratorConfig] for this [IngestionConfig].
     * The enumerator provides the elements to ingest.
     */
    val enumerator: EnumeratorConfig,

    /**
     * The [DecoderConfig] for this [IngestionConfig].
     * The [decoder] is staged between the [enumerator] providing elements for ingestion
     * and the [operators], processing the decoded elements.
     */
    val decoder: DecoderConfig,

    /**
     * The [OperatorConfig]s as a named map.
     * Provides named definitions of [OperatorConfig]s for the [operations] property.
     */
    val operators: Map<String, OperatorConfig>,

    /**
     * The [OperationsConfig]s as named map.
     * Pipeline representation as a named, ordered list of Operations.
     */
    val operations: Map<String, OperationsConfig>
)
