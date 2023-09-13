package org.vitrivr.engine.base.features.external

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.base.features.averagecolor.AverageColor
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class ExternalImageRetriever(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>, val queryVector: FloatVectorDescriptor
) : Retriever<ImageContent, FloatVectorDescriptor> {
    /*
    CHECK: currently same as in AverageColorRetriever
     Similarity search on FloatVectorDescriptor --> create utils?
    */
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = ProximityQuery(this.queryVector) //FIXME type system confusion?
        return flow {
            reader.getAll(query).forEach { emit(it) }
        }
    }

    companion object {
        fun score(query: FloatVectorDescriptor, target: FloatVectorDescriptor): Float {
            //TODO compute score
            //FIXME how to use already computed distance from database?

            return 0f
        }
    }

}