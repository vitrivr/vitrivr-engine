package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import kotlinx.serialization.json.JsonObject

/**
 * Holds generated description text and its model, prompt version, and generation metadata.
 *
 * @author Rahel Arnold
 */
data class VlmDescription(
    val text: String,
    val model: String,
    val promptVersion: String,
    val generationMetadata: JsonObject
)
