package org.vitrivr.engine.core.config.operators

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Enumerator


/**
 * Configuration element for an [Enumerator] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Deprecated(
    message = "Replaced by the new extraction pipeline definition language in package package org.vitrivr.engine.core.config.ingest",
    replaceWith = ReplaceWith("org.vitrivr.engine.core.config.ingest.operator.EnumeratorConfig"),
    level = DeprecationLevel.WARNING
)
@Serializable
data class EnumeratorConfig(
    val name: String,
    val api: Boolean = false,
    val parameters: Map<String, String> = emptyMap(),
    val next: DecoderConfig
)
