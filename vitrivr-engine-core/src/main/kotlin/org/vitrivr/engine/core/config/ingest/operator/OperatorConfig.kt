package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.Aggregator

/**
 * Configuration for ingestion operators, as defined in the [org.vitrivr.engine.core.operators.ingest] package.
 * This is the definition of the operators, whereas the [OperationsConfig] defines the actual pipeline
 */
@Serializable(with = OperatorConfigSerializer::class)
sealed class OperatorConfig {
    abstract val type: OperatorType
    abstract val parameters: Map<String, String>
}

/**
 * Configuration for a [Decoder].
 */
@Serializable
data class DecoderConfig(override val parameters: Map<String, String>): OperatorConfig(){
    override val type = OperatorType.DECODER
}

/**
 * Configuration for a [Transformer].
 */
@Serializable
data class TransformerConfig(override val parameters: Map<String, String>): OperatorConfig(){
    override val type = OperatorType.TRANSFORMER
}

/**
 * Configuration for an [Extractor].
 */
@Serializable
data class ExtractorConfig(
    val fieldName: String,
    val factory: String,
    override val parameters: Map<String, String>
): OperatorConfig(){
       override val type = OperatorType.EXTRACTOR
}

/**
 * Configuration for an [Exporter].
 */
@Serializable
data class ExporterConfig(
    val exporterName: String,
    val factory: String,
    override val parameters: Map<String, String>
): OperatorConfig(){
    override val type = OperatorType.EXPORTER
}

/**
 * Configuration for an [Aggregator].
 */
@Serializable
data class AggregatorConfig(
    val factory: String,
    override val parameters: Map<String, String>
): OperatorConfig(){
    override val type = OperatorType.AGGREGATOR
}
