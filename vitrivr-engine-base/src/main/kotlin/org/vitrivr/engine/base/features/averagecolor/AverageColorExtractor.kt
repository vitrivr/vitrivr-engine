package org.vitrivr.engine.base.features.averagecolor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBFloatColorContainer
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [Extractor] implementation for the [AverageColor] analyser.
 *
 * @see [AverageColor]
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class AverageColorExtractor(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    override val input: Operator<Ingested>,
    override val persisting: Boolean = true,
) : Extractor<ImageContent, FloatVectorDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Ingested> {
        val writer = if (this.persisting) {
            this.field.getWriter()
        } else {
            null
        }
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent) {
                val content = retrievable.content.filterIsInstance<ImageContent>()
                val descriptors = (this.field.analyser as? AverageColor)?.analyse(content) ?: emptyList()
                val color = MutableRGBFloatColorContainer()

                descriptors.forEach {
                    color += RGBFloatColorContainer(it.vector)
                }

                val descriptor = FloatVectorDescriptor(
                    retrievableId = retrievable.id,
                    transient = !persisting,
                    vector = listOf(
                        color.red / descriptors.size,
                        color.green / descriptors.size,
                        color.blue / descriptors.size
                    )
                )

                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    retrievable.addDescriptor(descriptor)
                }
                writer?.add(descriptor)
            }
            retrievable
        }
    }
}