package org.vitrivr.engine.features.external.torchserve

import com.google.protobuf.ByteString
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.features.external.torchserve.basic.TorchServe
import org.vitrivr.engine.features.external.torchserve.basic.TorchServeExtractor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.reflect.KClass

/**
 * A [TorchServe] implementation for image labelling.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TSImageLabel : TorchServe<ImageContent, LabelDescriptor>() {

    companion object {
        const val TORCHSERVE_THRESHOLD_KEY = "host"
    }

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass: KClass<LabelDescriptor> = LabelDescriptor::class

    /**
     * Generates a prototypical [LabelDescriptor] for this [TSImageLabel].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [LabelDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", 0.0f, null)

    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [TSImageLabel].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [StructBooleanRetriever] instance for this [TSImageLabel]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, LabelDescriptor>, query: Query, context: QueryContext): StructBooleanRetriever<ImageContent, LabelDescriptor> {
        require(query is SimpleBooleanQuery<*>) { "TSImageLabel only supports boolean queries." }
        return StructBooleanRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [StructBooleanRetriever] instance for this [TSImageLabel].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [LabelDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, LabelDescriptor>, descriptors: Collection<LabelDescriptor>, context: QueryContext): StructBooleanRetriever<ImageContent, LabelDescriptor> {
        val values = descriptors.map { it.label }
        val query = SimpleBooleanQuery(values.first(), ComparisonOperator.EQ, LabelDescriptor.LABEL_FIELD_NAME) /* TODO: An IN query would make more sense here. */
        return this.newRetrieverForQuery(field, query, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [TSImageLabel].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [Retriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, LabelDescriptor>, content: Collection<ImageContent>, context: QueryContext): Retriever<ImageContent, LabelDescriptor> {
        val host = field.parameters[TORCHSERVE_HOST_KEY] ?: TORCHSERVE_HOST_DEFAULT
        val port = field.parameters[TORCHSERVE_PORT_KEY]?.toIntOrNull() ?: TORCHSERVE_PORT_DEFAULT
        val token = field.parameters[TORCHSERVE_TOKEN_KEY]
        val model = field.parameters[TORCHSERVE_MODEL_KEY] ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        val threshold = field.parameters[TORCHSERVE_THRESHOLD_KEY]?.toFloatOrNull() ?: 0.0f
        val descriptors = this.analyse(content, model, host, port, token).filter({ it.confidence.value >= threshold })
        return this.newRetrieverForDescriptors(field, descriptors, context)
    }

    /**
     * Generates and returns a new [TorchServeExtractor] instance for this [TorchServe].
     *
     * @param field The [Schema.Field] to create an [TorchServeExtractor] for.
     * @param input The [Operator] that acts as input to the new [TorchServeExtractor].
     * @param context The [IndexContext] to use with the [TorchServeExtractor].
     *
     * @return A new [TorchServeExtractor] instance for this [Analyser]
     */
    override fun newExtractor(field: Schema.Field<ImageContent, LabelDescriptor>, input: Operator<Retrievable>, context: IndexContext): TSImageLabelExtractor {
        val host = context.local[field.fieldName]?.get(TORCHSERVE_HOST_KEY) ?: field.parameters[TORCHSERVE_HOST_KEY] ?: "127.0.0.1"
        val port = ((context.local[field.fieldName]?.get(TORCHSERVE_PORT_KEY) ?: field.parameters[TORCHSERVE_PORT_KEY]))?.toIntOrNull() ?: 7070
        val token = context.local[field.fieldName]?.get(TORCHSERVE_TOKEN_KEY) ?: field.parameters[TORCHSERVE_TOKEN_KEY]
        val model = context.local[field.fieldName]?.get(TORCHSERVE_MODEL_KEY) ?: field.parameters[TORCHSERVE_MODEL_KEY] ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        val threshold = (context.local[field.fieldName]?.get(TORCHSERVE_THRESHOLD_KEY) ?: field.parameters[TORCHSERVE_THRESHOLD_KEY])?.toFloatOrNull() ?: 0.0f
        return TSImageLabelExtractor(threshold, host, port, token, model, input, this, field, field.fieldName)
    }

    /**
     * Generates and returns a new [TorchServeExtractor] instance for this [TorchServe].
     *
     * @param name The name of the [TorchServeExtractor].
     * @param input The [Operator] that acts as input to the new [TorchServeExtractor].
     * @param context The [IndexContext] to use with the [TorchServeExtractor].
     *
     * @return A new [TorchServeExtractor] instance for this [TorchServe]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): TSImageLabelExtractor {
        val host = context.local[name]?.get(TORCHSERVE_HOST_KEY) ?: "127.0.0.1"
        val port = context.local[name]?.get(TORCHSERVE_PORT_KEY)?.toIntOrNull() ?: 7070
        val token = context.local[name]?.get(TORCHSERVE_TOKEN_KEY)
        val model = context.local[name]?.get(TORCHSERVE_MODEL_KEY) ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        val threshold = context.local[name]?.get(TORCHSERVE_THRESHOLD_KEY)?.toFloatOrNull() ?: 0.0f
        return TSImageLabelExtractor(threshold, host, port, token, model, input, this, null, name)
    }

    /**
     * Converts the [ImageContent] to a [ByteString].
     *
     * @param content [ImageContent] to convert.
     * @return [Map] containing the [ByteString] representation of the [ImageContent].
     */
    override fun toByteString(content: ImageContent): Map<String, ByteString> {
        /* Convert image if necessary. */
        val originalImage = content.content
        val imageWithoutAlpha = if (originalImage.type == BufferedImage.TYPE_INT_RGB) {
            originalImage
        } else {
            val newImage = BufferedImage(originalImage.width, originalImage.height, BufferedImage.TYPE_INT_RGB)
            val graphics = newImage.createGraphics()
            graphics.drawImage(originalImage, 0, 0, null)
            graphics.dispose()
            newImage
        }

        /* Write image to byte array. */
        val output = ByteArrayOutputStream()
        ImageIO.write(imageWithoutAlpha, "JPEG", output)
        return mapOf("data" to ByteString.copyFrom(output.toByteArray()))
    }

    /**
     * Converts a [ByteString] to a [List] of [LabelDescriptor]s.
     *
     * @param byteString [ByteString] to convert.
     * @return Map containing the [ByteString] representation of the [ImageContent].
     */
    override fun byteStringToDescriptor(byteString: ByteString): List<LabelDescriptor> {
        val decoded = Json.decodeFromString<Map<String, Float>>(byteString.toString(Charsets.UTF_8))
        return decoded.map { (label, confidence) -> LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), label, confidence, null) }
    }
}