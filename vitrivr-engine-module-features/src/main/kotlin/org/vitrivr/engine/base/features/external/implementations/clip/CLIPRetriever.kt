package org.vitrivr.engine.base.features.external.implementations.clip

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.util.math.ScoringFunctions

/**
 * [CLIPRetriever] implementation for external CLIP image feature retrieval.
 *
 * @param field Schema field for which the retriever operates.
 * @param query The query vector for proximity-based retrieval.
 * @param context The [QueryContext] used to execute the query with.
 *
 * @see [AbstractRetriever]
 * @see [ProximityQuery]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPRetriever(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: ProximityQuery<*>, context: QueryContext) : AbstractRetriever<ContentElement<*>, FloatVectorDescriptor>(field, query, context) {
    override fun toFlow(scope: CoroutineScope) = flow {
        this@CLIPRetriever.reader.getAll(this@CLIPRetriever.query).forEach {
            it.addAttribute(ScoringFunctions.max(it))
            emit(it)
        }
    }
}
