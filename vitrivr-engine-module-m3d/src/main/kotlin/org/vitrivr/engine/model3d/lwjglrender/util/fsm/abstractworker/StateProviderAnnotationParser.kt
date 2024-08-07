package org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker

import org.vitrivr.engine.model3d.lwjglrender.util.datatype.Variant
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.State
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Transition
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

/**
 * This class is used to parse the annotations of a state provider
 * @see StateProviderAnnotationParser.runTransitionMethods
 */
class StateProviderAnnotationParser() {
    /**
     * This method is used to invoke annotated methods of a state provider
     * It invokes all of provided Object methods which are annotated with [StateTransition], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter] or [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave]
     * For this purpose the caller has to provide the current state, the state which is leaved and the current transition
     * The sequence of the method invocation is the following:
     *
     *  1.  Check if the object is a state provider (has the [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateProvider] annotation)
     *  1.  Get all methods which are annotated with [StateTransition], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter] or [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave]
     *  1.  Invoke the methods with the provided data
     *
     *
     * @see org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateProvider
     *
     * @see StateTransition
     *
     * @see org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter
     *
     * @see org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave
     *
     *
     * @param object the instance of the state provider
     * @param leavedState the state which is leaved
     * @param enteredState the state which is entered
     * @param currentTransition the current transition (a state action pair)
     * @param data the data which is passed to the state provider methods
     * @throws InvocationTargetException if the method cannot be invoked
     * @throws IllegalAccessException if the method cannot be accessed
     */
    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    fun runTransitionMethods(
        `object`: Any,
        leavedState: State,
        enteredState: State,
        currentTransition: Transition,
        data: Variant
    ) {
        this.checkIfStateProvider(`object`)
        val methods = this.getTransitionRelatedMethods(`object`, leavedState, enteredState, currentTransition)
        for (method: Method in methods) {
            val params = ArrayList<Any>()
            val paramNames = getMethodRelatedParams(method)
            for (name: String? in paramNames) {
                params.add(data.get(Any::class.java, name!!))
            }
            method.isAccessible = true
            try {
                method.invoke(`object`, *params.toTypedArray())
            } catch (ex: IllegalArgumentException) {
                throw StateProviderException("The method " + method.name + " has the wrong parameters" + params)
            }
        }
    }

    /**
     * Checks if the provided object is a state provider
     * @param object the object as instance of worker implementation
     * @throws StateProviderException if the object is not a state provider
     */
    @Throws(StateProviderException::class)
    private fun checkIfStateProvider(`object`: Any) {
        if (Objects.isNull(`object`)) {
            throw StateProviderException("StateProvider is null")
        }
        val clazz: Class<*> = `object`.javaClass
        if (!clazz.isAnnotationPresent(StateProvider::class.java)) {
            throw StateProviderException(
                "The class "
                        + clazz.simpleName
                        + " is not a state provider"
            )
        }
    }

    /**
     * Returns all methods which are annotated with [StateTransition], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter] or [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave]
     * such that the method is related to the provided state and transition.
     * Which means that the method has to be invoked on this state transition
     * @param object the instance of the state provider
     * @param leavedState the state which is leaved
     * @param enteredState the state which is entered
     * @param currentTransition the current transition (a state action pair)
     * @return All methods which have to be invoked on this state transition
     */
    private fun getTransitionRelatedMethods(
        `object`: Any, leavedState: State, enteredState: State,
        currentTransition: Transition
    ): List<Method> {
        val methods = LinkedList<Method>()
        val clazz: Class<*> = `object`.javaClass
        for (method: Method in clazz.declaredMethods) {
            if (this.shouldInvokeMethod(method, leavedState, enteredState, currentTransition)) {
                methods.add(method)
            }
        }
        return methods
    }

    /**
     * Helper method which returns true if a given method is related to the provided state and transition
     * Which means that the method has to be invoked on this state transition
     * @param method the method which is checked
     * @param leavedState  the state which is leaved
     * @param enteredState  the state which is entered
     * @param currentTransition the current transition (a state action pair)
     * @return true if the method has to be invoked on this state transition
     */
    private fun shouldInvokeMethod(
        method: Method, leavedState: State, enteredState: State,
        currentTransition: Transition
    ): Boolean {
        if (method.isAnnotationPresent(StateTransition::class.java)) {
            val at = method.getAnnotation(
                StateTransition::class.java
            )
            return this.shouldInvokeMethod(at, currentTransition)
        } else if (method.isAnnotationPresent(StateEnter::class.java)) {
            val at = method.getAnnotation(
                StateEnter::class.java
            )
            return this.shouldInvokeMethod(at, enteredState)
        } else if (method.isAnnotationPresent(StateLeave::class.java)) {
            val at = method.getAnnotation(
                StateLeave::class.java
            )
            return this.shouldInvokeMethod(at, leavedState)
        }
        return false
    }

    /**
     * Helper method for StateTransition annotation
     * @param at the annotation of the method
     * @param currentTransition the current transition (a state action pair)
     * @return true if the method has to be invoked on this transition
     */
    private fun shouldInvokeMethod(at: StateTransition, currentTransition: Transition): Boolean {
        return Transition(State(at.state), Action(at.action)).equals(currentTransition)
    }

    /**
     * Helper method for StateEnter annotation
     * @param at  the annotation of the method
     * @param enteredState the state which is entered
     * @return true if the method has to be invoked on this state enter
     */
    private fun shouldInvokeMethod(at: StateEnter, enteredState: State): Boolean {
        return State(at.state).equals(enteredState)
    }

    /**
     * Helper method for StateLeave annotation
     * @param at the annotation of the method
     * @param enteredState the state which is entered
     * @return true if the method has to be invoked on this state leave
     */
    private fun shouldInvokeMethod(at: StateLeave, enteredState: State): Boolean {
        return State(at.state).equals(enteredState)
    }

    /**
     * Returns the names of parameters of a method which is annotated with [StateTransition], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter] or [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave]
     * @param method the method which is checked
     * @return List of parameter names
     */
    private fun getMethodRelatedParams(method: Method): List<String> {
        if (method.isAnnotationPresent(StateLeave::class.java)) {
            val at = method.getAnnotation(
                StateLeave::class.java
            )
            return Arrays.stream(at.data).toList()
        } else if (method.isAnnotationPresent(StateTransition::class.java)) {
            val at = method.getAnnotation(
                StateTransition::class.java
            )
            return Arrays.stream(at.data).toList()
        } else if (method.isAnnotationPresent(StateEnter::class.java)) {
            val at = method.getAnnotation(
                StateEnter::class.java
            )
            return Arrays.stream(at.data).toList()
        }
        throw StateProviderException("The method " + method.name + " is not a state provider method")
    }
}