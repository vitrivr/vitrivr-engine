package org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker

import java.io.Serial

/**
 * Exception thrown by the [StateProviderAnnotationParser] if an error occurs during parsing.
 */
@Suppress("unused")
class StateProviderException : RuntimeException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private val serialVersionUID = 2621424721657557641L
    }
}