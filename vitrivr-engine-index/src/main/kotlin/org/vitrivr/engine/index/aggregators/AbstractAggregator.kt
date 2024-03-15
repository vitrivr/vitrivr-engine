package org.vitrivr.engine.index.aggregators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Aggregator

/**
 * An abstract [Aggregator] implementation.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractAggregator(override val input: Operator<Retrievable>, protected val context: IndexContext) : Aggregator {
    /**
     *  Creates a flow for this [AbstractAggregator].
     *
     *  The default [AbstractAggregator] simply copies the incoming [Ingested] and replaces the content with the aggregated content.
     *
     *  @param scope [CoroutineScope] to use for the [Flow].
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map {
        val content = it.filteredAttributes(ContentAttribute::class.java).map { a -> a.content }
        if (content.isNotEmpty()) {
            val aggregated = this.aggregate(content)
            it.removeAttributes(ContentAttribute::class.java)
            aggregated.forEach { c -> it.addAttribute(ContentAttribute(c)) }
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