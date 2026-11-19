package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import org.vitrivr.engine.core.model.content.element.ImageContent

/**
 * Defines description generation for an image.
 *
 * @author Rahel Arnold
 */
interface VlmService {
    fun describe(image: ImageContent): VlmDescription
}
