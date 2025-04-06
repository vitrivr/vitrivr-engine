package org.vitrivr.engine.core.operators.sinks

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Sink] that terminates the execution of a [Retrievable] stream without any further processing.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class DefaultSink(override val input: Operator<Retrievable>, override val name: String, private val log: Boolean = false): Operator.Sink<Retrievable> {
    /** The [KLogger] used by this [DefaultSink]. */
    private val logger: KLogger = KotlinLogging.logger("DefaultSink#${this.name}")

    /**
     * Converts this [DefaultSink] to a [Flow] of [Unit].
     *
     * @param scope The [CoroutineScope] to use.
     */
    override fun toFlow(scope: CoroutineScope): Flow<Unit> = flow {
        var counter = 0L
        this@DefaultSink.input.toFlow(scope).collect { retrievable ->
            if (this@DefaultSink.log) this@DefaultSink.logger.info { "Successfully processed retrievable ${retrievable.id}." }
            counter++
        }
    }
}