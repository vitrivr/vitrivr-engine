package org.vitrivr.engine.base.features.averagecolor

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.color.RGBFloatColorContainer
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.util.extension.getRGBArray
import java.util.*

/**
 * Implementation of the [AverageColor] [Analyser], which derives the average color from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AverageColor: Analyser<ImageContent, FloatVectorDescriptor> {
    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [AverageColor].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), listOf(0.0f, 0.0f, 0.0f), true)

    /**
     *
     */
    override fun newExtractor(field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean): AverageColorExtractor {
        require(field.analyser == this) { "" }
        return AverageColorExtractor(field, input, persisting)
    }

    /**
     *
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>, context: QueryContext): AverageColorRetriever {
        require(field.analyser == this) { }
        return this.newRetrieverForDescriptors(field, this.analyse(content), context)
    }

    /**
     *
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): AverageColorRetriever {
        require(field.analyser == this) { }
        return AverageColorRetriever(field, descriptors.first(), context)
    }

    /**
     * Performs the [AverageColor] analysis on the provided [List] of [ImageContent] elements.
     *
     * @param content The [List] of [ImageContent] elements.
     * @return [List] of [FloatVectorDescriptor]s.
     */
    fun analyse(content: Collection<ImageContent>): List<FloatVectorDescriptor> = content.map {
        val color = MutableRGBFloatColorContainer()
        val rgb = it.content.getRGBArray()
        rgb.forEach { c -> color += RGBByteColorContainer.fromRGB(c) }

        /* Generate descriptor. */
        val averageColor = RGBFloatColorContainer(color.red / rgb.size, color.green / rgb.size, color.blue / rgb.size)
        FloatVectorDescriptor(UUID.randomUUID(), null, averageColor.toList(), true)
    }
}