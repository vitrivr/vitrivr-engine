package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import java.io.IOException

/**
 * Indicates that a VLM request failed or that its response did not contain a usable description.
 *
 * @author Rahel Arnold
 */
class VlmRequestException(message: String, cause: Throwable? = null) : IOException(message, cause)
