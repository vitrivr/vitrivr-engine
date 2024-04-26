package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.*

/**
 * Configuration for ingestion operators, as defined in the [org.vitrivr.engine.core.operators.ingest] package.
 * This is the definition of the operators, whereas the [OperationsConfig] defines the actual pipeline
 */
@Serializable(with = OperatorConfigSerializer::class)
sealed class OperatorConfig {


    /**
     * The [OperatorType] of the ingestion operator described by this config.
     */
    abstract val type: OperatorType

    /**
     * Additional parameters, operator dependent.
     */
    abstract val parameters: Map<String, String>
}

sealed class FactoryBuildableOperatorConfig : OperatorConfig() {
    /**
     * The class name of the factory for the corresponding operator.
     * See [org.vitrivr.engine.core.operators.ingest] for the available factories.
     */
    abstract val factory: String
}

/**
 * Configuration for a [Decoder].
 */
@Serializable
data class DecoderConfig(
    override val factory: String,
    override val parameters: Map<String, String> = mapOf()
) : FactoryBuildableOperatorConfig() {
    override val type = OperatorType.DECODER
}

/**
 * Configuration for a [Enumerator]
 */
@Serializable
data class EnumeratorConfig(
    override val factory: String,
    override val parameters: Map<String, String> = mapOf()
) : FactoryBuildableOperatorConfig() {
    override val type = OperatorType.ENUMERATOR
}


/**
 * Configuration for a [Transformer].
 */
@Serializable
data class TransformerConfig(
    override val factory: String, override val parameters: Map<String, String> = mapOf()
) : FactoryBuildableOperatorConfig() {
    override val type = OperatorType.TRANSFORMER
}

/**
 * Configuration for a [Segmenter].
 */
@Serializable
data class SegmenterConfig(
    override val factory: String, override val parameters: Map<String, String> = mapOf()
) : FactoryBuildableOperatorConfig() {
    override val type = OperatorType.SEGMENTER
}

/**
 * Configuration for an [Extractor].
 */
@Serializable
data class ExtractorConfig(
    /**
     * Name of a field as defined in the schema.
     */
    val fieldName: String,
    val factory: String? = null,
    override val parameters: Map<String, String> = mapOf(),

    ) : OperatorConfig() {
    override val type = OperatorType.EXTRACTOR

}

/**
 * Configuration for an [Exporter].
 */
@Serializable
data class ExporterConfig(
    /**
     * Name of an exporter as defined in the schema
     */
    val exporterName: String? = null,
    val factory: String? = null, override val parameters: Map<String, String> = mapOf()
) : OperatorConfig() {
    override val type = OperatorType.EXPORTER

    init {
        require(
            (exporterName.isNullOrBlank() && !factory.isNullOrBlank())
                    || (!exporterName.isNullOrBlank() && factory.isNullOrBlank())
        )
        { "An ExporterConfig must have either an exporter name (defined in the schema) or a factory name" }
    }
}

/**
 * Configuration for an [Aggregator].
 */
@Serializable
data class AggregatorConfig(
    override val factory: String, override val parameters: Map<String, String> = mapOf()
) : FactoryBuildableOperatorConfig() {
    override val type = OperatorType.AGGREGATOR
}
