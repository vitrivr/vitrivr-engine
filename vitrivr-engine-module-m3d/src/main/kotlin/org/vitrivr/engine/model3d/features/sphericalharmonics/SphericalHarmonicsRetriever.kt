package org.vitrivr.engine.model3d.features.sphericalharmonics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ModelContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
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
class SphericalHarmonicsRetriever(override val field: Schema.Field<ModelContent, FloatVectorDescriptor>, private val query: ProximityQuery<*>, private val context: QueryContext) : Retriever<ModelContent, FloatVectorDescriptor> {
    companion object {
        private const val MAXIMUM_DISTANCE = 1.0f
        fun scoringFunction(retrieved: Retrieved) : Float {
            val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return 0f
            return 1f - (distance / MAXIMUM_DISTANCE)
        }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {

        val reader = this.field.getReader()
        return flow {
            reader.queryAndJoin(this@SphericalHarmonicsRetriever.query).forEach {
                it.addAttribute(ScoreAttribute.Similarity(scoringFunction(it)))
                emit(it)
            }
        }
    }
}