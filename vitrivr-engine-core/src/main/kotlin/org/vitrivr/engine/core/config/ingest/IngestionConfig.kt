@file:OptIn(ExperimentalSerializationApi::class)

package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.vitrivr.engine.core.config.ingest.operation.OperationConfig
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.transform.shape.MergeType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Configuration of an ingestion pipeline for indexing.
 *
 * @author Loris Sauter
 * @author Ralph Gasser
 * @version 1.0.0
 *
 * @see IngestionPipelineBuilder
 */
@Serializable
data class IngestionConfig(
    /** The name of the [Schema] for this [IngestionConfig] */
    val schema: String,

    /** The [IngestionContextConfig] for this [IngestionConfig]'s context. */
    val context: IngestionContextConfig,

    /**
     * The [OperatorConfig]s as a named map.
     *
     * Provides named definitions of [OperatorConfig]s for the [operations] property.
     */
    val operators: Map<String, OperatorConfig>,

    /**
     * The [OperationConfig]s as named map.
     *
     * Pipeline representation as a named, ordered list of Operations.
     */
    val operations: Map<String, OperationConfig>,

    /** List of output operations. */
    val output: List<String>,

    /** The [MergeType] for the output operations. */
    val mergeType: MergeType? = null
) {


    companion object {
        /** The default config path for [IngestionConfig], which is `./config-index.json` */
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
