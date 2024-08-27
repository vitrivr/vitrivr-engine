package org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker

/**
 * With this annotation a method can be marked as a state enter method.
 * The method will be called when the state is entered.
 * The method must have the same number of parameters as the data array.
 * The parameters must be in the data container with the same key.
 * `@StateEnter(state = "STATENAME", data = {"dataKey1", "dataKey2"} `
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class StateEnter(val state: String, val data: Array<String> = [])