package org.vitrivr.engine.core.api.rest

data class ErrorStatusException(val statusCode: Int, override val message: String) : Exception(message) {

    val error: ErrorStatus
        get() = ErrorStatus(message)

}
