package org.vitrivr.engine.core.config

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

/**
 * A serializable configuration object to configure vitrivr engine.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
@Serializable
data class VitrivrConfig(
    /** List of [SchemaConfig] managed by this [VitrivrConfig]. */
    val schemas: List<SchemaConfig>
) {
    companion object {
        /** Default path to fall back to. */
        const val DEFAULT_SCHEMA_PATH = "./config.json"

        /**
         * Tries to read a [VitrivrConfig] from a file specified by the given [Path].
         *
         * @param path The [Path] to read [VitrivrConfig] from.
         * @return [VitrivrConfig] or null, if an error occurred.
         */
        fun read(path: Path): VitrivrConfig? = try {
            Files.newInputStream(path, StandardOpenOption.READ).use {
                Json.decodeFromStream<VitrivrConfig>(it)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Failed to read configuration from $path due to an exception." }
            null
        }
    }
}