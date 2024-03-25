package org.vitrivr.engine.core.config.pipeline.execution

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * An execution environment for data ingest and retrieval.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ExecutionServer {

    /** The [ExecutorService] used to execution [] */
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    /** The [CoroutineDispatcher] used for execution. */
    private val dispatcher: CoroutineDispatcher = this.executor.asCoroutineDispatcher()

    /** A [ConcurrentHashMap] of all ongoing [Job]s. */
    private val jobs = ConcurrentHashMap<UUID, Job>()

    /** A [ConcurrentHashMap] of all ongoing [Job]s. */
    private val jobHistory = Collections.synchronizedList(ArrayList<Triple<UUID, ExecutionStatus, Long>>(100))

    /**
     * Checks the status of the [Job] with the provided [UUID].
     *
     * @param uuid [UUID] of the [Job] to check.
     * @return [ExecutionStatus] of [Job]
     */
    fun status(uuid: UUID): ExecutionStatus {
        /* Check if job is still running. */
        val jobs = this.jobs[uuid]
        if (jobs != null) {
            return ExecutionStatus.RUNNING
        }

        /* Check list for job. */
        for (job in this.jobHistory) {
            if (job.first == uuid) {
                return job.second
            }
        }

        /* Otherwise, job is unknown. */
        return ExecutionStatus.UNKNOWN
    }

    /**
     * Cancels the [Job] with the provided [UUID].
     *
     * @param uuid [UUID] of the [Job] to check.
     * @return True, if job is running, false otherwise.
     */
    fun cancel(uuid: UUID): Boolean {
        val job = this.jobs[uuid]
        return if (job != null) {
            job.cancel()
            true
        } else {
            false
        }
    }

    /**
     * Executes an extraction [IndexingPipeline] in a blocking fashion, i.e., the call will block until the [IndexingPipeline] has been executed.
     *
     * This is mainly for testing purposes!
     *
     * @param pipeline The [IndexingPipeline] to execute.
     */
    fun extract(pipeline: IndexingPipeline) {
        val jobId = UUID.randomUUID()
        val scope = CoroutineScope(this@ExecutionServer.dispatcher) + CoroutineName("index-job-$jobId")
        runBlocking {
            val jobs = pipeline.getLeaves().map { e -> scope.launch { e.toFlow(this).takeWhile { it != AbstractSegmenter.TerminalRetrievable }.collect() } }
            jobs.forEach { it.join() }
        }
    }

    /**
     * Executes an [IndexingPipeline] in a blocking fashion, i.e., the call will block until the [IndexingPipeline] has been executed.
     *
     * @param pipeline The [IndexingPipeline] to execute.
     * @return [UUID] identifying the job.
     */
    fun extractAsync(pipeline: IndexingPipeline): UUID {
        val jobId = UUID.randomUUID()
        val scope = CoroutineScope(this@ExecutionServer.dispatcher) + CoroutineName("index-job-$jobId")
        val job = scope.launch {
            try {
                val jobs = pipeline.getLeaves().map { e -> this.launch { e.toFlow(scope).cancellable().takeWhile { it != AbstractSegmenter.TerminalRetrievable }.collect() } }
                jobs.forEach { it.join() }
                this@ExecutionServer.jobHistory.add(Triple(jobId, ExecutionStatus.COMPLETED, System.currentTimeMillis()))
            } catch (e: Throwable) {
                this@ExecutionServer.jobHistory.add(Triple(jobId, ExecutionStatus.FAILED, System.currentTimeMillis()))
            } finally {
                this@ExecutionServer.jobs.remove(jobId)
                if (this@ExecutionServer.jobHistory.size > 100) {
                    this@ExecutionServer.jobHistory.removeFirst()
                }
            }

        }
        this.jobs[jobId] = job
        return jobId
    }

    /**
     * Executes a [RetrievalPipeline] in a blocking fashion, i.e., the call will block until the [IndexingPipeline] has been executed.

     * @param query The [Operator] to execute.
     * @return The resulting [List] of [Retrieved]
     */
    fun query(query: Operator<Retrieved>): List<Retrieved> {
        val jobId = UUID.randomUUID()
        val scope = CoroutineScope(this@ExecutionServer.dispatcher) + CoroutineName("query-job-$jobId")
        val results = LinkedList<Retrieved>()
        runBlocking {
            val job = scope.launch {
                query.toFlow(scope).toList(results)
            }
            job.join()
        }
        return results
    }

    /**
     * Executes an [RetrievalPipeline] in an asynchronous fashion, sending all results to the provided [SendChannel].
     *
     * @param query The [Operator] to execute.
     * @param into The [SendChannel] to send the results to.
     * @return The [UUID] of the resulting [Job].
     */
    fun queryAsync(query: Operator<Retrieved>, into: SendChannel<Retrieved>): UUID {
        val jobId = UUID.randomUUID()
        val scope = CoroutineScope(this@ExecutionServer.dispatcher) + CoroutineName("query-job-$jobId")
        scope.launch {
            try {
                val job = this.launch { query.toFlow(scope).collect { into.send(it) } }
                this@ExecutionServer.jobHistory.add(Triple(jobId, ExecutionStatus.COMPLETED, System.currentTimeMillis()))
                job.join()
            } catch (e: Throwable) {
                this@ExecutionServer.jobHistory.add(Triple(jobId, ExecutionStatus.FAILED, System.currentTimeMillis()))
            } finally {
                this@ExecutionServer.jobs.remove(jobId)
                if (this@ExecutionServer.jobHistory.size > 100) {
                    this@ExecutionServer.jobHistory.removeFirst()
                }
            }

        }
        return jobId
    }
}