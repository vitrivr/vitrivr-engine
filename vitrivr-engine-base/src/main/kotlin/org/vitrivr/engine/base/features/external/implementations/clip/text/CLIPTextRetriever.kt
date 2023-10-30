package org.vitrivr.engine.base.features.external.implementations.clip.text

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.base.features.external.ExternalRetriever
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved

/**
 * [CLIPTextRetriever] implementation for external CLIP text feature retrieval.
 *
 * @param field Schema field for which the retriever operates.
 * @param query The query vector for proximity-based retrieval.
 *
 * @see [ExternalRetriever]
 * @see [ProximityQuery]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPTextRetriever(
    field: Schema.Field<TextContent, FloatVectorDescriptor>,
    query: FloatVectorDescriptor,
    context: QueryContext
) : ExternalRetriever<TextContent, FloatVectorDescriptor>(field, query, context) {

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        val query = ProximityQuery(this@CLIPTextRetriever.query)
        this@CLIPTextRetriever.reader.getAll(query).forEach {
            emit(it)
        }
    }
}
