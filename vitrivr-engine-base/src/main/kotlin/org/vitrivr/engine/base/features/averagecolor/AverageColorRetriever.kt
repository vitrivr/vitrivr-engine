package org.vitrivr.engine.base.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for the [AverageColor] analyser.
 *
 * @see [AverageColor]
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class AverageColorRetriever(override val field: Schema.Field<ImageContent,FloatVectorDescriptor>, val queryVector: FloatVectorDescriptor) : Retriever<ImageContent,FloatVectorDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = ProximityQuery(this.queryVector) //FIXME type system confusion?
        return flow {
            reader.getAll(query).forEach { emit(it) }
        }
    }

    companion object {
        fun score(query: FloatVectorDescriptor, target: FloatVectorDescriptor) : Float {
            //TODO compute score
            //FIXME how to use already computed distance from database?

            return 0f
        }
    }


}