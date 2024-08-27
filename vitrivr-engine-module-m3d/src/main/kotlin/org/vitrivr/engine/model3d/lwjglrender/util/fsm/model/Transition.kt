package org.vitrivr.engine.model3d.lwjglrender.util.fsm.model

/**
 * StateTransition describes a transition out of a currentState with a command Therefor it consists
 * of a state action pair The state action pair creates a unique hashcode
 */
class Transition
/**
 * Constructor for StateTransition
 *
 * @param state (current / outgoing) state of the transition
 * @param command command which triggers the transition from state
 */
(
    /** (current / outgoing) state of the transition */
    val state: State,
    /** command which triggers the transition from state */
    val command: Action
) {
  /**
   * Returns the (current / outgoing) state of the transition
   *
   * @return current state
   */
  /**
   * Returns the command of the transition
   *
   * @return command
   */

  /**
   * Creates a Unique hashcode for combination current state and command
   *
   * @return a hashCode for StateTransition
   */
  override fun hashCode(): Int {
    return 17 + 31 * state.hashCode() + 31 * command.hashCode()
  }

  /**
   * Implements the equals method for state Transition
   *
   * @param obj to compare
   * @return true if equal
   */
  override fun equals(obj: Any?): Boolean {
    if (obj === this) {
      return true
    }
    if (obj !is Transition) {
      return false
    }
    return state.equals(obj.state) && command.equals(obj.command)
  }

  override fun toString(): String {
    return "Transition{state=$state, command=$command}"
  }
}
