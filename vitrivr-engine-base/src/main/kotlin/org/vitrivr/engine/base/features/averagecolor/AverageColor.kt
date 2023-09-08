package org.vitrivr.engine.base.features.averagecolor

import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.color.RGBFloatColorContainer
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.model.util.toDescriptorList
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.util.extension.getRGBArray
import java.util.*

/**
 * Implementation of the [AverageColor] [Analyser], which derives the average color from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AverageColor: Analyser<ImageContent,FloatVectorDescriptor> {
    override val analyserName: String = "AverageColor"
    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [AverageColor].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), true, listOf(0.0f, 0.0f, 0.0f))

    /**
     *
     */
    override fun newExtractor(field: Schema.Field<ImageContent,FloatVectorDescriptor>, input: Operator<IngestedRetrievable>, persisting: Boolean): AverageColorExtractor {
        require(field.analyser == this) { "" }
        return AverageColorExtractor(field, input, persisting)
    }

    /**
     *
     */
    override fun newRetriever(field: Schema.Field<ImageContent,FloatVectorDescriptor>, content: Collection<ImageContent>): AverageColorRetriever {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content))
    }

    /**
     *
     */
    override fun newRetriever(field: Schema.Field<ImageContent,FloatVectorDescriptor>, descriptors: DescriptorList<FloatVectorDescriptor>): AverageColorRetriever {
        require(field.analyser == this) { }
        return AverageColorRetriever(field, descriptors.first())
    }

    /**
     * Performs the [AverageColor] analysis on the provided [List] of [ImageContent] elements.
     *
     * @param content The [List] of [ImageContent] elements.
     * @return [List] of [FloatVectorDescriptor]s.
     */
    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> = content.map {
        val color = MutableRGBFloatColorContainer()
        val rgb =  it.image.getRGBArray()
        rgb.forEach { c -> color += RGBByteColorContainer.fromRGB(c) }

        /* Generate descriptor. */
        val averageColor = RGBFloatColorContainer(color.red / rgb.size, color.green / rgb.size, color.blue / rgb.size)
        FloatVectorDescriptor(UUID.randomUUID(), null, false, averageColor.toList())
    }.toDescriptorList()
}