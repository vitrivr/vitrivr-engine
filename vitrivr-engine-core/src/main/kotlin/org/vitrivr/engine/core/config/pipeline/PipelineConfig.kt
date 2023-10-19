package org.vitrivr.engine.core.config.pipeline

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}


@Serializable
data class PipelineConfig (val schema: String, val context: ContextConfig,  val enumerator: EnumeratorConfig) {
    companion object {
        /** Default path to fall back to. */
        const val DEFAULT_PIPELINE_PATH = "./config-pipeline.json"

        /**
         * Tries to read a [PipelineConfig] from a file specified by the given [Path].
         *
         * @param path The [Path] to read [PipelineConfig] from.
         * @return [PipelineConfig] or null, if an error occurred.
         */
        fun read(path: Path): PipelineConfig? = try {
            Files.newInputStream(path, StandardOpenOption.READ).use {
                Json.decodeFromStream<PipelineConfig>(it)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Failed to read configuration from $path due to an exception." }
            null
        }
    }
}