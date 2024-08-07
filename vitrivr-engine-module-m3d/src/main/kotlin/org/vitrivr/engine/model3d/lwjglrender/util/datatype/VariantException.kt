package org.vitrivr.engine.model3d.lwjglrender.util.datatype

import java.io.Serial

/**
 * Exception thrown by the [Variant] class.
 * Used to indicate that a value could not be converted to the requested type.
 * Used to indicate that a key is not valid.
 */
@Suppress("unused")
class VariantException : RuntimeException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private val serialVersionUID = 3713210701207037554L
    }
}