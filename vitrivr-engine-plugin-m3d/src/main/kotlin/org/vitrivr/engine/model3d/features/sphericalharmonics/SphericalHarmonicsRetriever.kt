package org.vitrivr.engine.model3d.features.sphericalharmonics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * Implementation of an [Retriever], which derives leverages spherical harmonics for meshes as proposed in [1].
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *  A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class SphericalHarmonicsRetriever(override val field: Schema.Field<Model3DContent, FloatVectorDescriptor>, private val query: FloatVectorDescriptor, private val context: QueryContext) : Retriever<Model3DContent, FloatVectorDescriptor> {
    companion object {
        private const val MAXIMUM_DISTANCE = 1.0f
        fun scoringFunction(retrieved: Retrieved) : Float {
            val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return 0f
            return 1f - (distance / MAXIMUM_DISTANCE)
        }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val k = this.context.getProperty(this.field.fieldName, "limit")?.toIntOrNull() ?: 1000 //TODO get limit
        val returnDescriptor = this.context.getProperty(this.field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
        val reader = this.field.getReader()
        val query = ProximityQuery(value = this.query.vector, k = k, distance = Distance.EUCLIDEAN, fetchVector = returnDescriptor)
        return flow {
            reader.getAll(query).forEach {
                it.addAttribute(ScoreAttribute(scoringFunction(it)))
                emit(it)
            }
        }
    }
}