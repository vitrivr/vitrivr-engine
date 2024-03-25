package org.vitrivr.engine.base.features.averagecolor

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for the [AverageColor] analyser.
 *
 * @see [AverageColor]
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class AverageColorRetriever(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    private val query: ProximityQuery<Value.Float>
) : Retriever<ImageContent, FloatVectorDescriptor> {

    private val logger: KLogger = KotlinLogging.logger {}

    companion object {
        private const val MAXIMUM_DISTANCE = 3f
        fun scoringFunction(retrieved: Retrieved): Float {
            val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return 0f
            return 1f - (distance / MAXIMUM_DISTANCE)
        }
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        val reader = this@AverageColorRetriever.field.getReader()
        logger.debug { "Flow init with query $query" }
        reader.getAll(this@AverageColorRetriever.query).forEach {
            it.addAttribute(ScoreAttribute(scoringFunction(it)))
            emit(it)
        }
    }
}
