package org.vitrivr.engine.model3d.lwjglrender.util.fsm.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Graph
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.State
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Transition

/**
 * Implements a FiniteStateMachine
 *
 *
 * Inspired from Design pattern presented on Stackoverflow
 *
 * @see [
 * https://stackoverflow.com/questions/5923767/simple-state-machine-example-in-c/a>
](https://stackoverflow.com/questions/5923767/simple-state-machine-example-in-c) */
class FiniteStateMachine(
    /**
     * Hashtable for Unique State transitions
     */
    private val graph: Graph
) {
    var LOGGER: Logger = LogManager.getLogger(FiniteStateMachine::class.java)
    /**
     * @return current State of FSM
     */
    /**
     * The current State
     */
    var currentState: State?
        private set

    /**
     * Constructs a [FiniteStateMachine]
     * Sets the initial state
     * @param graph the graph which contains all states and transitions
     * and the initial state
     */
    init {
        this.currentState = graph.initialState()
    }


    /**
     * Gives a preview on next state with a hypothetical command
     *
     * @param command given hypothetical command
     * @return NextState resulting State
     * @throws FiniteStateMachineException if transition is not valid
     */
    @Throws(FiniteStateMachineException::class)
    fun previewNextState(command: Action): State? {
        val transition = Transition(
            currentState!!, command
        )
        if (graph.containsTransition(transition)) {
            return graph.getNextState(transition)
        } else {
            LOGGER.error("FSM transition to next state failed!")
            throw FiniteStateMachineException("Invalid transition: " + currentState.toString() + " -> " + command.toString())
        }
    }

    /**
     * Moves the FSM to the next state
     *
     * @param command given command
     * @return the resulting state after transition
     * @throws FiniteStateMachineException if transition is not valid
     */
    @Throws(FiniteStateMachineException::class)
    fun moveNextState(command: Action): Transition {
        val performedTransition = Transition(
            currentState!!, command
        )
        this.currentState = this.previewNextState(command)
        return performedTransition
    }
}