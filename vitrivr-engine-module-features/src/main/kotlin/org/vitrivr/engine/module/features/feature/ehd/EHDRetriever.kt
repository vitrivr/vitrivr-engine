package org.vitrivr.engine.module.features.feature.ehd

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
 * [Retriever] implementation for the [EHD] analyser.
 *
 * @see [EHD]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class EHDRetriever(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: ProximityQuery<Value.FloatVector>, context: QueryContext) : AbstractRetriever<ImageContent, FloatVectorDescriptor>(field, query, context) {

    companion object {
        /** [LinearCorrespondence] for [EHDRetriever]. Maximum distance is taken from Cineast implementation. */
        private val CORRESPONDENCE = LinearCorrespondence(16 / 4f)
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        this@EHDRetriever.reader.queryAndJoin(this@EHDRetriever.query).forEach {
            val distance = it.filteredAttribute<DistanceAttribute>()
            if (distance != null) {
                it.addAttribute(CORRESPONDENCE(distance))
            } else {
                this@EHDRetriever.logger.warn { "No distance attribute found for descriptor ${it.id}." }
            }
            emit(it)
        }
    }
}