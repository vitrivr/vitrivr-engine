package org.vitrivr.engine.base.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.Distance
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
class AverageColorRetriever(override val field: Schema.Field<ImageContent,FloatVectorDescriptor>, private val queryVector: FloatVectorDescriptor, private val k: Int = 1000, private val returnDescriptor: Boolean = false) : Retriever<ImageContent,FloatVectorDescriptor> {

    companion object {
        private const val MAXIMUM_DISTANCE = 3f
        fun scoringFunction(retrieved: Retrieved.RetrievedWithDistance) : Float = (retrieved.distance / MAXIMUM_DISTANCE)

    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = ProximityQuery(
            descriptor = this.queryVector,
            k = this.k,
            distance = Distance.MANHATTAN,
            returnDescriptor = returnDescriptor
        )
        return flow {
            reader.getAll(query).forEach {
                emit(
                    if (it is Retrieved.RetrievedWithDistance) {
                        Retrieved.PlusScore(it, scoringFunction(it))
                    } else {
                        it
                    }
                )
            }
        }
    }

}