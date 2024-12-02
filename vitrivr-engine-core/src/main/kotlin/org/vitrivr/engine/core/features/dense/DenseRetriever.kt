package org.vitrivr.engine.core.features.dense

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.math.correspondence.CorrespondenceFunction
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityPredicate
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * [DenseRetriever] implementation for proximity-based retrieval on float vector embeddings.
 *
 * @param field Schema field for which the retriever operates.
 * @param query The query vector for proximity-based retrieval.
 * @param context The [QueryContext] used to execute the query with.
 *
 * @see [AbstractRetriever]
 * @see [ProximityPredicate]
 *
 * @author Rahel Arnold
 * @author Fynn Faber
 * @version 1.0.0
 */
class DenseRetriever<C : ContentElement<*>>(field: Schema.Field<C, FloatVectorDescriptor>, query: Query, context: QueryContext, val correspondence: CorrespondenceFunction) : AbstractRetriever<C, FloatVectorDescriptor>(field, query, context) {
    override fun toFlow(scope: CoroutineScope) = flow {
        this@DenseRetriever.reader.queryAndJoin(this@DenseRetriever.query).forEach {
            val distance = it.filteredAttribute<DistanceAttribute>()
            if (distance != null) {
                it.addAttribute(this@DenseRetriever.correspondence(distance))
            } else {
                this@DenseRetriever.logger.warn { "No distance attribute found for descriptor ${it.id}." }
                it.addAttribute(ScoreAttribute.Similarity(0.0f))
            }
            emit(it)
        }
    }
}
