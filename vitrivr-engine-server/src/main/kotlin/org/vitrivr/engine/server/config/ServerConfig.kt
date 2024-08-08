package org.vitrivr.engine.server.config

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonObject
import org.vitrivr.engine.core.config.schema.SchemaConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}

@Serializable
data class ServerConfig(
    /** The configuration of the RESTful API. */
    val api: ApiConfig = ApiConfig(),

    /** List of [SchemaConfig] managed by this [ServerConfig]. */
    val schemas: Map<String, SchemaConfig>
) {
    companion object {
        /** Default path to fall back to. */
        const val DEFAULT_SCHEMA_PATH = "./config-schema.json"

        /**
         * Tries to read a [ServerConfig] from a file specified by the given [Path].
         *
         * @param path The [Path] to read [ServerConfig] from.
         * @return [ServerConfig] or null, if an error occurred.
         */
        fun read(path: Path): ServerConfig? = try {
            Files.newInputStream(path, StandardOpenOption.READ).use { inputStream ->
                val rawJson = inputStream.bufferedReader().use { it.readText() }
                val jsonObject = Json.parseToJsonElement(rawJson).jsonObject

                val apiConfig = jsonObject["api"]?.let {
                    Json.decodeFromJsonElement<ApiConfig>(ApiConfig.serializer(), it)
                } ?: ApiConfig()

                val schemasJsonObject = jsonObject["schemas"]?.jsonObject ?: return null
                val schemas = schemasJsonObject.map { (name, jsonElement) ->
                    name to SchemaConfig.fromJsonWithName(jsonElement, name)
                }.toMap()

                ServerConfig(api = apiConfig, schemas = schemas)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Failed to read configuration from $path due to an exception." }
            null
        }
    }
}
