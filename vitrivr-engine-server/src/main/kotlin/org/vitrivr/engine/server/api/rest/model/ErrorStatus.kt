package org.vitrivr.engine.server.api.rest.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorStatus(val message: String)
