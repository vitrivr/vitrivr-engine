package org.vitrivr.engine.base.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for the [AverageColor] analyser.
 *
 * @see [AverageColor]
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class AverageColorRetriever(override val field: Schema.Field<ImageContent, FloatVectorDescriptor>, private val queryVector: FloatVectorDescriptor, private val queryContext: QueryContext) : Retriever<ImageContent, FloatVectorDescriptor> {

    companion object {
        private const val MAXIMUM_DISTANCE = 3f
        fun scoringFunction(retrieved: Retrieved.RetrievedWithDistance) : Float = 1f - (retrieved.distance / MAXIMUM_DISTANCE)

    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {

        val k = queryContext.getProperty(this.field.fieldName, "limit")?.toIntOrNull() ?: 1000 //TODO get limit
        val returnDescriptor = queryContext.getProperty(this.field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        val reader = this.field.getReader()
        val query = ProximityQuery(
            descriptor = this.queryVector,
            k = k,
            distance = Distance.MANHATTAN,
            withDescriptor = returnDescriptor
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