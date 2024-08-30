package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.resolver.Resolver
import java.nio.file.Files
import java.nio.file.Paths


/**
 * A serializable configuration object to configure [Schema].
 *
 * @see [Schema]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class SchemaConfig(
    /** Name of the [Schema]. */
    var name: String = "vitrivr",

    /** The (database) [ConnectionConfig] for this [SchemaConfig]. */
    val connection: ConnectionConfig,

    /**
     * List of [FieldConfig]s that are part of this [SchemaConfig].
     */
    val fields: Map<String, FieldConfig>,

    /**
     * The list of [ResolverConfig]s that are part of this [SchemaConfig].
     * @see Resolver
     */
    val resolvers: Map<String, ResolverConfig> = emptyMap(),

    /**
     * List of [ExporterConfig]s that are part of this [SchemaConfig].
     * @see Exporter
     */
    val exporters: Map<String, ExporterConfig> = emptyMap(),

    /**
     * List of [PipelineConfig]s that are part of this [SchemaConfig].
     */
    val extractionPipelines: Map<String, PipelineConfig> = emptyMap()
) {

    companion object {
        /**
         * Tries to load a [SchemaConfig] from the resources.
         *
         * @param resourcePath Path to the resource.
         * @return [SchemaConfig]
         */
        fun loadFromResource(resourcePath: String): SchemaConfig {
            val json = Json { ignoreUnknownKeys = true } // Configure Json to ignore unknown keys
            val uri = this::class.java.classLoader.resources(resourcePath).findFirst().orElseThrow { IllegalArgumentException("Resource '$resourcePath' not found!") }.toURI()
            val path = Paths.get(uri)
            val jsonString = Files.readString(path)
            return json.decodeFromString<SchemaConfig>(jsonString)
        }
    }
}
