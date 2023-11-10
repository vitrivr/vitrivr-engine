package org.vitrivr.engine.index.config

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.vitrivr.engine.index.config.operators.EnumeratorConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}

/**
 * A configuraion object for an indexing job. Can be processed by the [PipelineBuilder].
 *
 * @see PipelineBuilder
 *
 * @author Raphael Waltenspül
 * @version 1.0.0
 */
@Serializable
data class IndexConfig(val schema: String, val context: ContextConfig, val enumerator: EnumeratorConfig) {
    companion object {
        /** Default path to fall back to. */
        const val DEFAULT_PIPELINE_PATH = "./config-pipeline.json"

        /**
         * Tries to read a [IndexConfig] from a file specified by the given [Path].
         *
         * @param path The [Path] to read [IndexConfig] from.
         * @return [IndexConfig] or null, if an error occurred.
         */
        fun read(path: Path): IndexConfig? = try {
            Files.newInputStream(path, StandardOpenOption.READ).use {
                Json.decodeFromStream<IndexConfig>(it)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Failed to read configuration from $path due to an exception." }
            null
        }
    }
}