package org.vitrivr.engine.base.features.external.implementations.CLIP_Image

import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * Implementation of the [CLIPImageFactory] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPImageFactory : ExternalAnalyser<ImageContent, FloatVectorDescriptor>() {
    override val analyserName: String = "CLIPImage"
    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class
    override val featureName: String = "/extract/clip_image"
    override val host: String = "localhost"
    override val port: Int = 8888

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIPImageFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() =
        FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), listOf(0.0f, 0.0f, 0.0f), true)


    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Ingested>, persisting: Boolean
    ): CLIPImageExtractor {
        require(field.analyser == this) { "" }
        return CLIPImageExtractor(field, input, persisting, host, port, featureName)
    }

    override fun newRetriever(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>
    ): CLIPImageRetriever {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content))
    }

    override fun newRetriever(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: DescriptorList<FloatVectorDescriptor>
    ): CLIPImageRetriever {
        require(field.analyser == this) { }
        return CLIPImageRetriever(field, descriptors.first())
    }


}