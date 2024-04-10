package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.Aggregator
import org.vitrivr.engine.core.operators.ingest.Enumerator

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

sealed class FactoryBuildableOperatorConfig: OperatorConfig() {
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
data class DecoderConfig(override val parameters: Map<String, String>, override val factory: String): FactoryBuildableOperatorConfig(){
    override val type = OperatorType.DECODER
}

/**
 * Configuration for a [Enumerator]
 */
@Serializable
data class EnumeratorConfig(override val parameters: Map<String, String>,
                            override val factory: String
): FactoryBuildableOperatorConfig(){
    override val type = OperatorType.ENUMERATOR
}


/**
 * Configuration for a [Transformer].
 */
@Serializable
data class TransformerConfig(override val parameters: Map<String, String>,
                             override val factory: String
): FactoryBuildableOperatorConfig(){
    override val type = OperatorType.TRANSFORMER
}

/**
 * Configuration for a [Segmenter].
 */
@Serializable
data class SegmenterConfig(override val parameters: Map<String, String>,
                             override val factory: String
): FactoryBuildableOperatorConfig(){
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
    val fieldName: String?,
    val factory: String?,
    override val parameters: Map<String, String>
): OperatorConfig(){
       override val type = OperatorType.EXTRACTOR

    init {
        require((fieldName.isNullOrBlank() && !factory.isNullOrBlank())
                || (!fieldName.isNullOrBlank() && factory.isNullOrBlank()) )
        {"An ExtractorConfig must have either a field name (referencing a schema's field) or a factory name"}
    }
}

/**
 * Configuration for an [Exporter].
 */
@Serializable
data class ExporterConfig(
    /**
     * Name of an exporter as defined in the schema
     */
    val exporterName: String?,
    val factory: String?,
    override val parameters: Map<String, String>
): OperatorConfig(){
    override val type = OperatorType.EXPORTER

    init {
        require((exporterName.isNullOrBlank() && !factory.isNullOrBlank())
                || (!exporterName.isNullOrBlank() && factory.isNullOrBlank()) )
        {"An ExporterConfig must have either an exporter name (defined in the schema) or a factory name"}
    }
}

/**
 * Configuration for an [Aggregator].
 */
@Serializable
data class AggregatorConfig(
    override val parameters: Map<String, String>, override val factory: String
): FactoryBuildableOperatorConfig(){
    override val type = OperatorType.AGGREGATOR
}
