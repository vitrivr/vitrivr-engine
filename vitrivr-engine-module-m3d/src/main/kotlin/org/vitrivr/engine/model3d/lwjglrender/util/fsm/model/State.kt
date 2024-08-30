package org.vitrivr.engine.model3d.lwjglrender.util.fsm.model

/**
 * Represents a State in a Finite State Machine
 * The State is uniquely identified by its name
 * The name is used to create a hashcode and to compare states
 */
class State
/**
 * Creates a new State with a unique name
 *
 * @param name of the state, should be unique
 */(
    /**
     * Unique Name of the state
     */
    private val name: String
) {
    /**
     * Creates a Unique hashcode for current state
     *
     * @return a hashCode for state
     */
    override fun hashCode(): Int {
        return 17 + 31 * name.hashCode()
    }

    /**
     * Implements the equals method for state
     *
     * @param obj to compare
     * @return true if the states are equal
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if ((obj !is State)) {
            return false
        }
        return this.name == obj.name
    }

    /**
     * Returns the unique name of the state
     *
     * @return name of the state
     */
    override fun toString(): String {
        return this.name
    }
}