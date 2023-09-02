package org.vitrivr.engine.index.execution

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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

    /**
     * Executes an extraction job using a [List] of [Extractor]s.
     *
     * @param extractors The [List] of [Extractor]s to execute.
     */
    fun extract(extractors: List<Extractor<*,*>>) = runBlocking {
        val scope = CoroutineScope(this@ExecutionServer.dispatcher)
        val jobs = extractors.map { e -> scope.launch { e.toFlow(scope).takeWhile { it != AbstractSegmenter.TerminalIngestedRetrievable }.collect() } }
        jobs.size
    }
}