package org.vitrivr.engine.base.features.external.implementations.DINO

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.base.features.external.ExternalRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery

/**
 * [DINORetriever] implementation for external DINO feature retrieval.
 *
 * @param field Schema field for which the retriever operates.
 * @param queryVector The query vector for proximity-based retrieval.
 *
 * @see [ExternalRetriever]
 * @see [ProximityQuery]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class DINORetriever(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    override val queryVector: FloatVectorDescriptor
) : ExternalRetriever<ImageContent, FloatVectorDescriptor>() {

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = ProximityQuery(queryVector)

        return flow {
            reader.getAll(query).forEach {
                emit(it)
            }
        }
    }
}
