package org.vitrivr.engine.base.features.external.implementations.dino

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
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
 * Implementation of the [DINO] [ExternalAnalyser], which derives the DINO feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class DINO : ExternalWithFloatVectorDescriptorAnalyser<ImageContent>() {
    companion object {
        /**
         * Static method to request the DINO feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the DINO feature descriptor.
         * @return A list of DINO feature descriptors.
         */
        fun requestDescriptor(content: ContentElement<*>): List<Float> {
            return DINO().httpRequest(content)
        }


    }

    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    // Default values for external API
    override val endpoint: String = "/extract/dino"
    override val host: String = "localhost"
    override val port: Int = 8888

    // Size and list for prototypical descriptor
    override val size = 384
    override val featureList = List(size) { 0.0f }


    /**
     * Requests the DINO feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the DINO feature descriptor.
     * @return A list of DINO feature descriptors.
     */
    override fun requestDescriptor(content: ContentElement<*>): List<Float> {
        val a = httpRequest(content)
        print(a)
        return a
    }

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DINO].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun newExtractor(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext,
        persisting: Boolean,
        parameters: Map<String, Any>
    ): Extractor<ImageContent, FloatVectorDescriptor> {
        require(field.analyser == this) { "" }
        logger.debug { "Creating new DINOExtractor for field '${field.fieldName}' with parameters $parameters." }
        return DINOExtractor(input, field, persisting)
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
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return this.newRetrieverForDescriptors(field, this.processContent(content), context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [DINO].
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
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return DINORetriever(field, descriptors.first(), context)
    }
}