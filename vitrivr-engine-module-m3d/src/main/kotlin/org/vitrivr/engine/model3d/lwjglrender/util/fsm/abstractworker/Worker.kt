package org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.controller.FiniteStateMachine
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.controller.FiniteStateMachineException
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Graph
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.State
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.BlockingDeque

/**
 * <h3>This Abstract Worker:</h3>
 * Worker is the abstract class for the concrete worker thread.
 * **The Abstract Worker provides:**
 *
 *  * Loading the [Graph] from concrete worker implementation
 *  * Creating a finite state machine [FiniteStateMachine]
 *  * Waiting on a [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.Job]
 *  * Performing a [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.Job], by perform [Action] and walk with the [FiniteStateMachine] through the [Graph]
 *  * On each transition a [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateProviderAnnotationParser] to invoke the marked methods
 * (with [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateTransition]) from the concrete worker implementation
 *  * Handling exceptions [StateProviderException] or [FiniteStateMachine]
 *
 *
 * This abstract worker has to be extended by a **concrete worker** implementation.
 *
 *
 * <h3>Concrete worker that extends from abstract Worker</h3>
 * The concrete worker has to implement all methods to do the concrete job `<T extends Job>`
 * If a method should be invoked on a state transition, the method has to be annotated with [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateTransition]
 * The graph has to be generated on instantiation of concrete worker. It has to describe all legal transitions.
 * Further it has to provide an initial state and a set of final states.
 *
 *
 * On each legal transition the [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateProviderAnnotationParser] invokes the methods that are annotated with [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateLeave], [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateTransition]
 * The variant data is passed to the method as parameter, related to the defined key in the annotation.
 *
 *
 * If a final state is reached, the job is finished and the worker waits for the next job.
 * The concrete worker implementation has to handle the result of the job and return a [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand] to the worker.
 *
 *
 * If an exception is thrown, the worker calls the method [.onJobException] from the concrete worker implementation.
 * The concrete worker implementation has to handle the exception and return a [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand] to the worker.
 *
 * @param <T> The type of the job
</T> */
@StateProvider
abstract class Worker<T : Job?>(
    /**
     * The queue of jobs that are waiting to be performed.
     * `<T>` is the type of concrete implementation of [Job]
     */
    private val jobs: BlockingDeque<T>
) :
    Runnable {
    /**
     * Flag to shut down the worker.
     * After that the worker has to be reinitialized.
     * Usually on shutdown of the application.
     */
    private var shutdown = false

    /**
     * The graph that describes the legal transitions for the concrete worker.
     * The finite state machine will walk through this graph.
     */
    private val graph: Graph

    /**
     * The current job that is performed.
     */
    protected var currentJob: T? = null

    /**
     * Constructor for the abstract worker.
     * Registers the queue of jobs that are waiting to be performed.
     * Calls the abstract method [.createGraph], which has to be implemented by the concrete worker.
     *
     * @param jobs The queue of jobs that are waiting to be performed.
     */
    init {
        this.graph = this.createGraph()
    }

    /**
     * Abstract method to create the graph.
     * The graph has to be generated on instantiation of concrete worker.
     * It has to describe all legal transitions.
     * Further it has to provide an initial state and a set of final states.
     *
     * @return The graph that describes the legal transitions for the concrete worker.
     */
    protected abstract fun createGraph(): Graph

    /**
     * Abstract method to handle the exception.
     * The concrete worker implementation has to handle the exception and return a [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand] to the worker.
     * After that the job is finished and the worker waits for the next job.
     *
     * @param ex The exception that was thrown.
     * @return The [JobControlCommand] to the worker.
     */
    protected abstract fun onJobException(ex: Exception?): String?

    /**
     * Worker thread loop.
     */
    override fun run() {
        this.shutdown = false
        // While application is running
        while (!this.shutdown) {
            try {
                //LOGGER.trace("Waiting for job. In Queue:" + this.jobs.size());
                LOGGER.debug(
                    "Perform job. In queue: {}",
                    jobs.size
                )
                when (currentJob!!.type) {
                    JobType.ORDER -> this.performJob(
                        this.currentJob!!
                    )

                    JobType.CONTROL -> {
                        this.shutdown = true
                        LOGGER.info("Worker is shutting down.")
                    }
                    JobType.RESPONSE -> LOGGER.error("Worker response not handled.")

                }
            } finally {
                LOGGER.trace("Worker has performed Job. In Queue:" + jobs.size)
            }
        }
    }

    /**
     * Job loop. Perform a single Job
     *
     *  1. Setup the Statemachine with initialized graph
     *  1. Gets the action sequence and the job data for this job
     *  1. Do till final state in graph is reached (or exception is thrown)
     *
     * A single job loop:
     *
     *  *  Take action
     *  *  Move to next state
     *  *  The StateProviderAnnotationParser will call all methods in the Worker that were marked with a corresponding Annotation
     *
     *
     * @param job Job to be performed.
     * @see StateEnter
     *
     * @see StateLeave
     *
     * @see StateTransition
     */
    fun performJob(job: T) {
        this.currentJob = job

        // Mark if job is finished
        var performed = false
        // Setup the Statemachine with initialized graph
        val fsm = FiniteStateMachine(this.graph)

        // Get the action queue
        val actions = job!!.actions
        // Get the job data
        val data = job.data

        while (!performed) {
            try {
                // Take next action
                val action = actions!!.take()
                // S_{i}, The current state S_{i} is the state before the transition
                val leavedState: State = fsm.currentState!!
                // S_{i} -> T_{k} -> S_{i+1} Move to next state, get the transition
                val currentTransition = fsm.moveNextState(action)
                // S_{i+1}, The current state S_{i+1} is the state after the transition
                val enteredState: State = fsm.currentState!!
                // Instantiate the StateProviderAnnotationParser and run it.
                val sap = StateProviderAnnotationParser()
                sap.runTransitionMethods(this, leavedState, enteredState, currentTransition, data!!)
                // Check if final state is reached
                performed = graph.isFinalState(enteredState)
            } catch (ex: FiniteStateMachineException) {
                // Exception is thrown if an illegal transition is performed
                LOGGER.error("Error in job transition. Abort: ", ex)
                this.onJobException(ex)
                performed = true
            } catch (ex: InterruptedException) {
                // Exception is thrown if the worker is interrupted (needed for blocking queue)
                LOGGER.error("Critical interruption in job task. Abort: ", ex)
                this.onJobException(ex)
                performed = true
            } catch (ex: InvocationTargetException) {
                // This exception is thrown if an exception is thrown during invocation of the methods in the concrete worker
                this.onJobException(ex)
                LOGGER.error("Error in concrete wWorker. Abort: ", ex)
                performed = true
            } catch (ex: IllegalAccessException) {
                this.onJobException(ex)
                LOGGER.error("Error in concrete wWorker. Abort: ", ex)
                performed = true
            } finally {
            }
        }
    }

    /**
     * Method to put an action at the end of the action queue.
     * Could be used to add an action to perform an error handling.
     *
     * @param action The action to be put at the end of the action queue.
     */
    @Suppress("unused")
    protected fun putActionFirst(action: Action?) {
        currentJob!!.actions!!.addFirst(action)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
}