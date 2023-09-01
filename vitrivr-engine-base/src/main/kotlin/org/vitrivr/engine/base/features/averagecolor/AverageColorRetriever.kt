package org.vitrivr.engine.base.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class AverageColorRetriever(
    override val field: Schema.Field<FloatVectorDescriptor>
) : Retriever<FloatVectorDescriptor> {

    override val describer: AverageColor = AverageColor()

    override fun toFlow(scope: CoroutineScope): Flow<ScoredRetrievable> {

        val reader = field.getReader()

        val queryVector = FloatVectorDescriptor(vector = listOf(), retrievableId = UUID.randomUUID()) //FIXME how to get the query information inside here?

        val query = ProximityQuery(queryVector) //FIXME type system confusion?

        return flow{
            reader.getAll(query).forEach {

                val scored = ScoredRetrievable.Default(
                    id = it.retrievableId,
                    transient = false,
                    parts = mutableSetOf(),
                    partOf = mutableSetOf(),
                    score = score(queryVector, it)
                )

                emit(scored)

            }
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