package org.vitrivr.engine.core.api.rest

import kotlinx.serialization.Serializable

@Serializable
data class ErrorStatus(val message: String)
