package org.vitrivr.engine.core.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for the [AverageColor] analyser.
 *
 * @see [AverageColor]
 *
 * @author Luca Rossetto
 * @version 1.2.0
 */
class AverageColorRetriever(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: ProximityQuery<Value.FloatVector>, context: QueryContext) : AbstractRetriever<ImageContent, FloatVectorDescriptor>(field, query, context) {

    companion object {
        /** [LinearCorrespondence] for [AverageColorRetriever]. */
        private val CORRESPONDENCE = LinearCorrespondence(3f)
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        this@AverageColorRetriever.reader.queryAndJoin(this@AverageColorRetriever.query).forEach {
            val distance = it.filteredAttribute<DistanceAttribute>()
            if (distance != null) {
                it.addAttribute(CORRESPONDENCE(distance))
            } else {
                this@AverageColorRetriever.logger.warn { "No distance attribute found for descriptor ${it.id}." }
            }
            emit(it)
        }
    }
}
