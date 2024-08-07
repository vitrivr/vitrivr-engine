package org.vitrivr.engine.model3d.lwjglrender.util.fsm.model

import org.vitrivr.engine.model3d.lwjglrender.util.datatype.Variant

/**
 * Represents an Action in a Finite State Machine
 * The Action is uniquely identified by its name
 * The name is used to create a hashcode and to compare actions
 * The Action can contain data
 * The data provides in an action is only visible to the state transition which were triggered by the action
 */
class Action {
    /**
     * Unique Name of the action
     */
    private val name: String

    /**
     * @return the data of the action
     */
    /**
     * Data of the action
     */
    val data: Variant?

    /**
     * Creates a new Action with a unique name
     *
     * @param name of the action, should be unique
     */
    constructor(name: String) {
        this.name = name
        this.data = null
    }

    /**
     * Creates a new Action with a unique name and additional data
     *
     * @param name of the action, should be unique
     * @param data of the action
     */
    constructor(name: String, data: Variant?) {
        this.name = name
        this.data = data
    }

    /**
     * @return true if the action has data
     */
    @Suppress("unused")
    fun hasData(): Boolean {
        return this.data != null
    }

    /**
     * Creates a Unique hashcode for command
     *
     * @return a hashCode for Command
     */
    override fun hashCode(): Int {
        return 17 + 31 * name.hashCode()
    }

    /**
     * Implements the equals method for state Transition
     *
     * @param obj to compare
     * @return true if the states are equal
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj !is Action) {
            return false
        }
        return this.name == obj.name
    }

    /**
     * Returns the unique name of the action
     *
     * @return name of the action
     */
    override fun toString(): String {
        return this.name
    }
}