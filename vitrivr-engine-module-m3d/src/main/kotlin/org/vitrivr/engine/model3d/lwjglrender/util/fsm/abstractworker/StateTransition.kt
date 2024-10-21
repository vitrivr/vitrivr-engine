package org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker

/**
 * With this annotation a method can be marked as a state transition method.
 * The method will be called when a specific transition is triggered.
 * (A transition is a state action pair)
 * The method must have the same number of parameters as the data array.
 * The parameters must be in the data container with the same key.
 * `@StateTransition(state = "STATENAME", action = "ACTION", data = {"dataKey1", "dataKey2"} `
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class StateTransition(val state: String, val action: String, val data: Array<String> = [])