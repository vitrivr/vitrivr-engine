package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.metamodel.Schema


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
         * List of [ExporterConfig]s that are part of this [SchemaConfig].
         */
        val exporters: List<ExporterConfig> = emptyList(),

        /**
         * List of [PipelineConfig]s that are part of this [SchemaConfig].
         */
        val extractionPipelines: List<PipelineConfig> = emptyList()
)