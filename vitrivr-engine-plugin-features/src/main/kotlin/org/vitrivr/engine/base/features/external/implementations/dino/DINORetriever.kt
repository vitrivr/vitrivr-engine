package org.vitrivr.engine.base.features.external.implementations.dino

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.base.features.external.implementations.clip.CLIPRetriever
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * [DINORetriever] implementation for external DINO feature retrieval.
 *
 * @param field Schema field for which the retriever operates.
 * @param query The query vector for proximity-based retrieval.
 *
 * @see [AbstractRetriever]
 * @see [ProximityQuery]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class DINORetriever(
    field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    query: FloatVectorDescriptor,
    context: QueryContext
) : AbstractRetriever<ImageContent, FloatVectorDescriptor>(field, query, context) {

    companion object {
        fun scoringFunction(retrieved: Retrieved): Float {
            val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return 0f
            return 1f - distance
        }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val k = context.getProperty(field.fieldName, "limit")?.toIntOrNull() ?: 1000 //TODO get limit
        val returnDescriptor =
            context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
        val reader = field.getReader()
        val query = ProximityQuery(
            descriptor = this@DINORetriever.query,
            k = k,
            distance = Distance.COSINE,
            withDescriptor = returnDescriptor
        )
        return flow {
            reader.getAll(query).forEach {
                it.addAttribute(ScoreAttribute(CLIPRetriever.scoringFunction(it)))
                emit(
                    it
                )
            }
        }
    }
}
