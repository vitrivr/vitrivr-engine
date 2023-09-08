package org.vitrivr.engine.base.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * [Extractor] implementation for the [AverageColor] analyser.
 *
 * @see [AverageColor]
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class AverageColorExtractor(
    override val field: Schema.Field<ImageContent,FloatVectorDescriptor>,
    override val input: Operator<IngestedRetrievable>,
    override val persisting: Boolean = true,
) : Extractor<ImageContent,FloatVectorDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<IngestedRetrievable> {
        val writer = if (this.persisting) { this.field.getWriter() } else { null }
        return this.input.toFlow(scope).map { retrievable: IngestedRetrievable ->
            val content = retrievable.content.filterIsInstance<ImageContent>()
            val descriptors = this.field.analyser.analyse(content)
            for (d in descriptors) {
                val copy = d.copy(retrievableId = retrievable.id)
                retrievable.addDescriptor(copy)
                writer?.add(copy)
            }
            retrievable
        }
    }
}