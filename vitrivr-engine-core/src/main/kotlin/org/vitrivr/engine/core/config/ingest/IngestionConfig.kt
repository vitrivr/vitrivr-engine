@file:OptIn(ExperimentalSerializationApi::class)

package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.vitrivr.engine.core.config.ContextConfig
import org.vitrivr.engine.core.config.ingest.operation.OperationsConfig
import org.vitrivr.engine.core.config.ingest.operator.DecoderConfig
import org.vitrivr.engine.core.config.ingest.operator.EnumeratorConfig
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.operators.ingest.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Configuration of an ingestion pipeline for indexing.
 *
 * Current strict order of operators
 * [Enumerator] -> [Decoder] -> ([Transformer] | [Segmenter])* - if [Segmenter] > [Aggregator]* -> ([Exporter] | [Extractor])*
 *
 * @see IngestionPipelineBuilder
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
) {


    companion object {
        /** The default config path for [IngestionConfig], which is `./config-index.json` */
        const val DEFAULT_PIPELINE_PATH = "./config-index.json"
        private val logger: KLogger = KotlinLogging.logger("IngestionConfig")


        /**
         * Reads a [IngestionConfig] from file located at [path].
         *
         * @param path The [Path] to read the [IngestionConfig] from.
         * @return [IngestionConfig] that has been parsed, or null in case of an exception
         */
        fun read(path: Path): IngestionConfig? = try {
            Files.newInputStream(path, StandardOpenOption.READ).use {
                Json.decodeFromStream<IngestionConfig>(it)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Failed to read the ingestion configuration from $path due to an exception." }
            null
        }

    }
}
