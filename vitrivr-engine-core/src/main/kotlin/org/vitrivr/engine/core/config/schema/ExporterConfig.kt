package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.operators.general.ExporterFactory
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [ExporterConfig] describes an [Exporter], how it is to be constructed by an [ExporterFactory].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class ExporterConfig(
    /**
     * The simple or fully qualified class name of the [ExporterFactory]
     */
    val factory: String,

    /**
     * A list of key-value pairs to further configure the [Exporter]
     */
    val parameters: Map<String, String> = emptyMap(),
)