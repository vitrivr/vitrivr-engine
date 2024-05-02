package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.resolver.Resolver


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
    val name: String = "vitrivr",

    /** The (database) [ConnectionConfig] for this [SchemaConfig]. */
    val connection: ConnectionConfig,

    /**
     * List of [FieldConfig]s that are part of this [SchemaConfig].
     */
    val fields: List<FieldConfig>,

    /**
     * The list of [ResolverConfig]s that are part of this [SchemaConfig].
     * @see Resolver
     */
    val resolvers: Map<String, ResolverConfig> = emptyMap(),

    /**
     * List of [ExporterConfig]s that are part of this [SchemaConfig].
     * @see Exporter
     */
    val exporters: List<ExporterConfig> = emptyList(),

    /**
     * List of [PipelineConfig]s that are part of this [SchemaConfig].
     */
    val extractionPipelines: List<PipelineConfig> = emptyList()
)
