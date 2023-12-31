package org.vitrivr.engine.base.features.external.implementations.dino

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved

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
class DINORetriever(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: FloatVectorDescriptor, context: QueryContext) : AbstractRetriever<ImageContent, FloatVectorDescriptor>(field, query, context) {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        val query = ProximityQuery(this@DINORetriever.query) /* TODO: Not sure, if the default setting should be used here. */
        this@DINORetriever.reader.getAll(query).forEach {
            emit(it)
        }
    }
}
