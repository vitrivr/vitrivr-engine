package org.vitrivr.engine.index.aggregators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithRelationship
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
        if (it is RetrievableWithContent) {
            val content = this.aggregate(it.content)
            val descriptors: List<Descriptor> = (it as? RetrievableWithDescriptor)?.descriptors ?: emptyList()
            val relationships = (it as? RetrievableWithRelationship)?.relationships ?: emptySet()
            Ingested(it.id, it.type, it.transient, content, descriptors, relationships)
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