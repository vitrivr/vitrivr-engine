package org.vitrivr.engine.module.features.feature.huehistogram

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
 * [Retriever] implementation for the [HueHistogram] analyser.
 *
 * @see [HueHistogram]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class HueHistogramRetriever(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: ProximityQuery<Value.FloatVector>, context: QueryContext) : AbstractRetriever<ImageContent, FloatVectorDescriptor>(field, query, context) {

    companion object {
        /** [LinearCorrespondence] for [HueHistogramRetriever]. Maximum distance is taken from Cineast implementation. */
        private val CORRESPONDENCE = LinearCorrespondence(16f)
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        this@HueHistogramRetriever.reader.queryAndJoin(this@HueHistogramRetriever.query).forEach {
            val distance = it.filteredAttribute<DistanceAttribute>()
            if (distance != null) {
                it.addAttribute(CORRESPONDENCE(distance))
            } else {
                this@HueHistogramRetriever.logger.warn { "No distance attribute found for descriptor ${it.id}." }
            }
            emit(it)
        }
    }
}