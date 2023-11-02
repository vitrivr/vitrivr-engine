package org.vitrivr.engine.index.execution

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.index.config.PipelineBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ExecutionServer {
    /** The [ExecutorService] used to execution [] */
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    /** The [CoroutineDispatcher] used for execution. */
    private val dispatcher: CoroutineDispatcher = this.executor.asCoroutineDispatcher()

    private lateinit var operators: List<Operator<*>>

    /**
     * Executes an extraction job using a [List] of [Extractor]s.
     *
     * @param extractors The [List] of [Extractor]s to execute.
     */
    fun extract(extractors: List<Operator<Retrievable>>) = runBlocking {
        val scope = CoroutineScope(this@ExecutionServer.dispatcher)
        val jobs = extractors.map { e -> scope.launch { e.toFlow(scope).takeWhile { it != AbstractSegmenter.TerminalRetrievable }.collect() } }
        jobs.forEach { it.join() }
    }


    fun addOperatorPipeline(operatorPipeline: PipelineBuilder){
        this.operators = operatorPipeline.getPipeline()
    }

    fun execute() = runBlocking {
        val scope = CoroutineScope(this@ExecutionServer.dispatcher)
        val jobs = this@ExecutionServer.operators.map { e -> scope.launch { e.toFlow(scope).takeWhile() { it != AbstractSegmenter.TerminalRetrievable }.collect() } }
        jobs.forEach { it.join() }
    }

    /**
     * Shuts down the [ExecutorService] used by this [ExecutionServer]
     */
    fun shutdown() {
        this.executor.shutdown()
    }
}