package org.vitrivr.engine.index.aggregators.content

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * An abstract [Transformer] implementation for aggregators; aggregators are used to aggregate the content of [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
abstract class AbstractAggregator(override val input: Operator<out Retrievable>, protected open val context: Context) : Transformer {
    /**
     *  Creates a flow for this [AbstractAggregator].
     *
     *  The default [AbstractAggregator] simply copies the incoming [Ingested] and replaces the content with the aggregated content.
     *
     *  @param scope [CoroutineScope] to use for the [Flow].
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map {
        if (it.content.isNotEmpty()) {
            val aggregated = this.aggregate(it.content)
            it.clearContent()
            aggregated.forEach { c -> it.addContent(c) }
            it
        } else {
            it
        }
    }

    /**
     * Performs aggregation on a [List] of [ContentElement]s.
     *
     * @param content The [List] of [ContentElement]s.
     */
    abstract fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>>
}