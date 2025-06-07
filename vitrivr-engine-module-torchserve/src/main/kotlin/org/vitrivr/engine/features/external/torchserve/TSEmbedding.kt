package org.vitrivr.engine.features.external.torchserve

import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteStringUtf8
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.BoundedCorrespondence
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.source.file.MimeType
import org.vitrivr.engine.features.external.torchserve.basic.TorchServe
import org.vitrivr.engine.features.external.torchserve.basic.TorchServeExtractor
import java.util.*
import kotlin.reflect.KClass

/**
 * [TorchServe] implementation for generic embedding models (e.g. CLIP, DINO).
 *
 * @author Laura Rettig
 * @version 1.0.0
 */
class TSEmbedding : TorchServe<ContentElement<*>, FloatVectorDescriptor>() {

    companion object {
        const val TORCHSERVE_DIMENSIONS = "dimensions"
    }

    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [LabelDescriptor] for this [TSImageLabel].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): FloatVectorDescriptor {
        val dimensions = field.parameters[TORCHSERVE_DIMENSIONS]?.toIntOrNull() ?: 512
        return FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(FloatArray(dimensions)))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [TSEmbedding].
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [DenseRetriever] instance for this [TSEmbedding]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: Query, context: QueryContext): DenseRetriever<ContentElement<*>> {
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.FloatVector>, context, BoundedCorrespondence(0.0, 2.0))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [TSEmbedding].
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [TSEmbedding]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        return this.newRetrieverForQuery(field, ProximityQuery(value = descriptors.first().vector, k = k, fetchVector = fetchVector), context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [TSEmbedding].
     *
     * @param field The [Schema.Field] to create a [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return A new [Retriever] instance for this [TSEmbedding]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val host = field.parameters[TORCHSERVE_HOST_KEY] ?: TORCHSERVE_HOST_DEFAULT
        val port = field.parameters[TORCHSERVE_PORT_KEY]?.toIntOrNull() ?: TORCHSERVE_PORT_DEFAULT
        val token = field.parameters[TORCHSERVE_TOKEN_KEY]
        val model = field.parameters[TORCHSERVE_MODEL_KEY] ?: throw IllegalArgumentException("Missing model for TorchServe model.")

        val vectors = this.analyse(content, model, host, port, token)

        return this.newRetrieverForDescriptors(field, vectors, context)
    }

    /**
     * Generates and returns a new [TorchServeExtractor] instance for this [TorchServe].
     *
     * @param field The [Schema.Field] to create an [TorchServeExtractor] for.
     * @param input The [Operator] that acts as input to the new [TorchServeExtractor].
     * @param context The [IndexContext] to use with the [TorchServeExtractor].
     *
     * @return A new [TorchServeExtractor] instance for this [TorchServe]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext): TorchServeExtractor<ContentElement<*>, FloatVectorDescriptor> {
        val host = context.local[field.fieldName]?.get(TORCHSERVE_HOST_KEY) ?: field.parameters[TORCHSERVE_HOST_KEY] ?: "127.0.0.1"
        val port = ((context.local[field.fieldName]?.get(TORCHSERVE_PORT_KEY) ?: field.parameters[TORCHSERVE_PORT_KEY]))?.toIntOrNull() ?: 7070
        val token = context.local[field.fieldName]?.get(TORCHSERVE_TOKEN_KEY) ?: field.parameters[TORCHSERVE_TOKEN_KEY]
        val model = context.local[field.fieldName]?.get(TORCHSERVE_MODEL_KEY) ?: field.parameters[TORCHSERVE_MODEL_KEY] ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        return TorchServeExtractor(host, port, token, model, input, this, field, field.fieldName)
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
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): TorchServeExtractor<ContentElement<*>, FloatVectorDescriptor> {
        val host = context.local[name]?.get(TORCHSERVE_HOST_KEY) ?: "127.0.0.1"
        val port = context.local[name]?.get(TORCHSERVE_PORT_KEY)?.toIntOrNull() ?: 7070
        val token = context.local[name]?.get(TORCHSERVE_TOKEN_KEY)
        val model = context.local[name]?.get(TORCHSERVE_MODEL_KEY)
        ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        return TorchServeExtractor(host, port, token, model, input, this, null, name)
    }

    /**
     * Converts the [ImageContent] to a [ByteString].
     */
    override fun toByteString(content: ContentElement<*>): Map<String, ByteString> =  when (content) {
        is ImageContent -> mapOf("data" to content.toDataUrl(MimeType.JPEG).toByteStringUtf8())
        is TextContent -> mapOf("data" to ByteString.copyFrom(content.content.toByteArray(Charsets.UTF_8)))
        else -> throw IllegalArgumentException("Unsupported content type: ${content::class}")
    }

    /**
     * Converts a [ByteString] (TorchServe model output) to a [FloatVectorDescriptor].
     *
     * Assumes TorchServe model returns a JSON array of floats, e.g. [0.1, 0.2, ...]
     */
    override fun byteStringToDescriptor(byteString: ByteString): List<FloatVectorDescriptor> {
        val jsonElement = Json.parseToJsonElement(byteString.toString(Charsets.UTF_8))
        val floatArray = jsonElement.jsonArray.map { it.toString().toFloat() }.toFloatArray()
        return listOf(FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(floatArray)))
    }
}