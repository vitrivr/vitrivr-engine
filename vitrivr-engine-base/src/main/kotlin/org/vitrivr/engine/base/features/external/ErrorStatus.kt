package org.vitrivr.engine.base.features.external

import kotlinx.serialization.Serializable

/**
 * An [ErrorStatus] as returned by the Feature Extraction Server
 *
 * @author Rahel Arnold
 */
@Serializable
data class ErrorStatus(val code: Int, val description: String)