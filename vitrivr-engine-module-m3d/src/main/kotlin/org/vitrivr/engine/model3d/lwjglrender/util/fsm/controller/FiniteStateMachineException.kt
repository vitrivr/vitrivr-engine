package org.vitrivr.engine.model3d.lwjglrender.util.fsm.controller

import java.io.Serial

/**
 * Exception thrown by the FiniteStateMachine if an illegal state transition is attempted.
 */
@Suppress("unused")
class FiniteStateMachineException : Exception {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private val serialVersionUID = 500983167704077039L
    }
}