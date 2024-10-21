package org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.vitrivr.engine.model3d.lwjglrender.util.datatype.Variant
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

/**
 * A Job is a container for data and actions. A Job can be of type ORDER, RESPONSE or CONTROL
 *
 *  * ORDER: Needs action, data and provides a result queue
 *  * RESPONSE: A job which contains data which the worker calculated
 *  * CONTROL: A job which contains a command e.g. end of job or error etc.
 *
 *
 *
 * [JobType]
 *
 *
 * If a job is a CONTROL job it contains a command
 * [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand]
 */
abstract class Job {
    /**
     * The queue contains the action sequence of the job that the worker has to perform.
     * The actions can be added before registering the job to the worker or on the fly.
     * @return The actions queue of the job.
     */
    /**
     * The actions to perform These actions are performed by the worker
     *
     *
     * The actions can be added before registering the job to the worker
     *
     *
     * The actions can also be added on the fly
     */
    val actions: BlockingDeque<Action>?

    /**
     * The result queue is used to provide results in a ORDER job
     */
    private val resultQueue: BlockingDeque<Job>?
    /**
     * @return The data of the job. Null if the job is a CONTROL job
     */
    /**
     * Sets the data of the job as a variant.
     * The key of the data has to match the data string in the annotation.
     * @see StateEnter,
     *
     * @see StateLeave
     *
     * @see StateTransition
     * On a transitions in the StateMachine, the parser search the data that has to be passed to the invoked method by its key and hand it over as a parameter.
     *
     * @param data The data to set
     */
    /**
     * The data to process The resulting data
     */
    var data: Variant? = null
    /**
     * The type of the job is used to control the worker
     * Order jobs are something the worker has to do.
     * Response jobs is the result the worker has done.
     * Control jobs are commands for the worker.
     * @return The type of the job.
     */
    /**
     * The type of the job The type of the job can be ORDER, RESPONSE or CONTROL [JobType]
     */
    val type: JobType
    /**
     * The command is the detailed information of a control job.
     * @return The command of the job
     */
    /**
     * The command of the job if the job is a control job The command of the job can be END, ERROR or NONE [org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand]
     */
    val command: JobControlCommand?

    /**
     * Creates a new ORDER Job to perform a task.
     *
     *
     * ORDER: Needs actions, data and provides a result queue
     *
     * @param actions The actions to perform
     * @param data   The data to process
     */
    protected constructor(actions: BlockingDeque<Action>?, data: Variant?) {
        this.command = null
        this.actions = actions
        this.data = data
        this.type = JobType.ORDER
        this.resultQueue = LinkedBlockingDeque()
    }

    /**
     * Creates a new Response Job.
     *
     *
     * RESPONSE: contains data which the worker calculated
     *
     * @param data The resulting data
     */
    protected constructor(data: Variant?) {
        this.actions = null
        this.command = null
        this.data = data
        this.type = JobType.RESPONSE
        this.resultQueue = null
    }

    /**
     * Creates a new Control Job
     *
     *
     * CONTROL: contains a command e.g. end of job or error etc.
     * @param command The command of the job
     */
    protected constructor(command: JobControlCommand?) {
        this.actions = null
        this.command = command
        this.type = JobType.CONTROL
        this.resultQueue = null
    }

    @get:Throws(InterruptedException::class)
    val results: Job
        /**
         * Returns the result of the job
         *
         *
         * This method blocks until a result is available
         *
         * @return The result of the job
         * @throws InterruptedException If the thread is interrupted while waiting for a result
         */
        get() {
            checkNotNull(this.resultQueue)
            return resultQueue.take()
        }

    /**
     * Puts a result into the result queue
     *
     * @param job The result to put into the result queue
     */
    fun putResultQueue(job: Job) {
        try {
            checkNotNull(this.resultQueue)
            resultQueue.put(job)
        } catch (ex: InterruptedException) {
            LOGGER.error("Error while putting result into result queue", ex)
        }
    }

    /**
     * Cleans the job
     *
     *
     * This method should be called after the job is processed
     *
     *
     * It does not affect data in the variant
     */
    fun clean() {
        data!!.clear()
        this.data = null
        if (this.actions != null) {
            actions.clear()
        }
        if (this.resultQueue != null) {
            resultQueue.clear()
        }
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
}