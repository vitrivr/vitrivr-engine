package org.vitrivr.engine.base.features.external.implementations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.retrieve.Retriever

class DenseEmbeddingRetriever(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: ProximityQuery<*>, context: QueryContext) : AbstractRetriever<ContentElement<*>, FloatVectorDescriptor>(field, query, context) {

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        TODO("Not yet implemented")
    }
}