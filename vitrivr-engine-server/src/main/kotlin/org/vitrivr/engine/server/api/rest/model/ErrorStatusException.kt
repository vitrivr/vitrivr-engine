package org.vitrivr.engine.server.api.rest.model

data class ErrorStatusException(val statusCode: Int, override val message: String) : Exception(message) {

    val error: ErrorStatus
        get() = ErrorStatus(message)

}
