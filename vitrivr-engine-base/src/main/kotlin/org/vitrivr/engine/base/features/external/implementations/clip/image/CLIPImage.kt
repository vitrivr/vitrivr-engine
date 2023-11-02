package org.vitrivr.engine.base.features.external.implementations.clip.image

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
import org.vitrivr.engine.base.features.external.implementations.dino.DINO
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Implementation of the [CLIPImage] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPImage : ExternalWithFloatVectorDescriptorAnalyser<ImageContent>() {

    companion object {
        /**
         * Static method to request the CLIP feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the CLIP feature descriptor.
         * @return A list of CLIP feature descriptors.
         */
        fun requestDescriptor(content: ContentElement<*>): List<Float> {
            return CLIPImage().httpRequest(content)
        }
    }

    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    // Default values for external API
    override val endpoint: String = "/extract/clip_image"
    override val host: String = "localhost"
    override val port: Int = 8888

    // Size and list for prototypical descriptor
    override val size = 512
    override val featureList = List(size) { 0.0f }

    /**
     * Requests the CLIP feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CLIP feature descriptor.
     * @return A list of CLIP feature descriptors.
     */
    override fun requestDescriptor(content: ContentElement<*>): List<Float> = httpRequest(content)

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIPImage].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), this.featureList, true)

    /**
     * Generates and returns a new [Extractor] instance for this [Analyser].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ImageContent, FloatVectorDescriptor> {
        require(field.analyser == this) { "" }
        logger.debug { "Creating new CLIPImageExtractor for field '${field.fieldName}' with parameters $parameters." }
        return CLIPImageExtractor(input, field, context, persisting)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [DINO].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>,
        content: Collection<ImageContent>,
        context: QueryContext
    ): Retriever<ImageContent, FloatVectorDescriptor> {
        return this.newRetrieverForDescriptors(field, this.processContent(content), context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [CLIPImage].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [Descriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>,
        descriptors: Collection<FloatVectorDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, FloatVectorDescriptor> {
        require(field.analyser == this) { }
        return CLIPImageRetriever(field, descriptors.first(), context)
    }
}