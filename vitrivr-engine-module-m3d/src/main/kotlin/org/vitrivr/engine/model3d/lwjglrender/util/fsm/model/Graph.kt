package org.vitrivr.engine.model3d.lwjglrender.util.fsm.model

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import kotlin.collections.HashMap

/**
 * Represents a graph that can be used by an FSM to traverse.
 */
class Graph @JvmOverloads constructor(
    /**
     * The transitions of the graph.
     */
    private val transitions: HashMap<Transition, State>,
    /**
     * The initial state of the graph.
     */
    private val initialState: State, goalStates: HashSet<State>,
    export: Boolean = false
) {
    /**
     * The set of goal states of the graph.*
     */
    private val goalStates: Set<State> =
        goalStates

    /**
     * @see Graph.Graph
     * @param export parameter to disable the export of the graph.
     */
    /**
     * Creates a new Graph.
     * The setup process can be as follows:
     * The transitions have to describe for each state which action leads to which state.
     *
     *
     * `return new Graph(new Hashtable<>() {{
     * {put(new Transition(new State("Startup"), new Action("wait")), new State("Startup"));}
     * {put(new Transition(new State("Startup"), new Action("print")), new State("Print"));}
     * ...
     * }}, new State("Startup"), new HashSet<>() {{
     * {add(new State("Result"));}
     * }});`
     *
     * @param transitions The transitions of the graph as a hashtable.
     * @param initialState The initial state of the graph.
     * @param goalStates The set of goal states of the graph.
     */
    init {
        if (export) {
            this.export()
        }
    }

    /**
     * Returns the initial state of the graph.
     *
     * @return The initial state of the graph.
     */
    fun initialState(): State {
        return this.initialState
    }

    /**
     * True if graph contains the transition
     * @param transition transition to check
     * @return true if graph contains the transition
     */
    fun containsTransition(transition: Transition): Boolean {
        return transitions.containsKey(transition)
    }

    /**
     * Returns the next state based on given transition which is a unique state action pair.
     * @param transition The transition to check.
     * @return The next state for a given transition.
     */
    fun getNextState(transition: Transition): State? {
        return transitions[transition]
    }

    /**
     * Returns true if the given state is a goal state.
     * @param enteredState The state to check.
     * @return True if the given state is a goal state.
     */
    fun isFinalState(enteredState: State): Boolean {
        return goalStates.contains(enteredState)
    }

    /**
     * Generates a graph viz string representation of the graph.
     */
    fun toString(flavour: String?): String {
        val sb = StringBuilder()
        sb.append("@startuml")
        sb.append("\n")
        sb.append("[*] --> ")
        sb.append(this.initialState)
        sb.append("\n")
        for ((key, value) in this.transitions) {
            sb.append(key.state.toString())
            sb.append(" --> ")
            sb.append(value.toString())
            sb.append(" : ")
            sb.append(key.command.toString())
            sb.append("\n")
        }
        for (entry in this.goalStates) {
            sb.append(entry.toString())
            sb.append(" --> [*]")
            sb.append("\n")
        }
        sb.append("@enduml")

        return sb.toString()
    }

    /**
     * Helper for exports the graph to a file.
     */
    private fun export() {
        val file = File("fsm.txt")
        try {
            Files.writeString(file.toPath(), this.toString("plantuml"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}