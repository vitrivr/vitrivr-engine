package org.vitrivr.engine.base.features.external

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for external feature retrieval.
 *
 * @see [ExternalAnalyser]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalRetriever(
    val queryVector: Descriptor
) : Retriever<ContentElement<*>, Descriptor> {

    abstract fun query(queryVector: Descriptor): Query<Descriptor>

    abstract fun parseFeatureResponse(response: String): FloatVectorDescriptor

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = query(queryVector)

        return flow {
            reader.getAll(query).forEach {
                emit(it)
            }
        }
    }
}
