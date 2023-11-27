package org.vitrivr.engine.core.config.pipeline.execution

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import org.vitrivr.engine.core.config.pipeline.Pipeline
import org.vitrivr.engine.core.config.pipeline.PipelineBuilder
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque


private val logger: KLogger = KotlinLogging.logger {}

/**
 * A
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ExecutionServer private constructor(schema: Schema){

    companion object {

        @Volatile private var instances: MutableMap<Schema, ExecutionServer> = mutableMapOf()

        fun getInstance(schema: Schema) =
            instances[schema] ?: synchronized(this) { // synchronized to avoid concurrency problem
                instances[schema]  ?: ExecutionServer(schema).also { instances[schema]  = it }
            }
    }


    /** The [ExecutorService] used to execution [] */
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    /** The [CoroutineDispatcher] used for execution. */
    private val dispatcher: CoroutineDispatcher = this.executor.asCoroutineDispatcher()

    var indexJobQueue: BlockingQueue<Pair<Pipeline,UUID>> = LinkedBlockingDeque()

    init {
        this.run()
    }

    fun isPending(uuid: UUID): Int {
        return this.indexJobQueue.indexOf(this.indexJobQueue.find { it.second == uuid })
    }

    fun enqueueIndexJob(pipeline: Pipeline): UUID {
        val uuid = UUID.randomUUID()
        return  this.enqueueIndexJob(pipeline, uuid)
    }

    fun enqueueIndexJob(pipeline: Pipeline, uuid: UUID): UUID{
        this.indexJobQueue.add(Pair(pipeline, uuid))
        return uuid;
    }

    /**
     * Executes an extraction job using a [List] of [Extractor]s.
     *
     * @param extractors The [List] of [Extractor]s to execute.
     */
    private fun extract(pipeline: Pipeline) = runBlocking {
        val scope = CoroutineScope(this@ExecutionServer.dispatcher)
        val jobs = pipeline.getLeaves().map { e -> scope.launch { e.toFlow(scope).takeWhile { it != AbstractSegmenter.TerminalRetrievable }.collect() } }
        jobs.forEach { it.join() }
    }


    private fun run() {
        Thread {
            val running = true
            while (running) {
                val pipeline = indexJobQueue.take()
                try {
                    this.extract(pipeline.first)
                    logger.debug { "Extraction with pipeline '${pipeline.second}' finished." }
                } catch (e: Exception) {
                    logger.error { "Error while executing extraction job: ${e.message}" }
                }
                // wait
                Thread.sleep(10000)
            }
        }.start()
    }

    /**
     * Shuts down the [ExecutorService] used by this [ExecutionServer]
     */
    fun shutdown() {
        this.executor.shutdown()
    }
}