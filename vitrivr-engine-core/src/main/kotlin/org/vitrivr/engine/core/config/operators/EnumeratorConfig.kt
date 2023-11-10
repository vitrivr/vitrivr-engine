package org.vitrivr.engine.core.config.operators

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Enumerator


/**
 * Configuration element for an [Enumerator] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Serializable
data class EnumeratorConfig(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
    val next: DecoderConfig
)