package org.vitrivr.engine.module.features.feature.huehistogram

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
import org.vitrivr.engine.core.model.color.HSVColorContainer
import org.vitrivr.engine.core.model.color.RGBColorContainer
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.util.extension.getRGBArray
import java.util.*
import kotlin.reflect.KClass

/**
 * A hue histogram [Analyser] for [ImageContent] objects.
 *
 * Migrated from Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class HueHistogram : Analyser<ImageContent, FloatVectorDescriptor> {

    companion object {
        private const val VECTOR_SIZE = 16
    }

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(ImageContent::class)
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [HueHistogram].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): FloatVectorDescriptor = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(VECTOR_SIZE))

    /**
     * Generates and returns a new [HueHistogramExtractor] instance for this [HueHistogram].
     *
     * @param name The name of the [HueHistogramExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = HueHistogramExtractor(input, this, name)

    /**
     * Generates and returns a new [HueHistogramExtractor] instance for this [HueHistogram].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext) = HueHistogramExtractor(input, this,  field)

    /**
     * Generates and returns a new [DenseRetriever] instance for this [HueHistogram].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [DenseRetriever] instance for this [HueHistogram]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: Query, context: QueryContext): DenseRetriever<ImageContent> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.FloatVector>, context, LinearCorrespondence(16f))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [HueHistogram].
     *
     * Invoking this method involves converting the provided [FloatVectorDescriptor] into a [ProximityQuery] that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): DenseRetriever<ImageContent> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = descriptors.first().vector, k = k, fetchVector = fetchVector), context)
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [HueHistogram].
     *
     * Invoking this method involves converting the provided [ImageContent] and the [QueryContext] into a [FloatVectorDescriptor]
     * that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>, context: QueryContext) = this.newRetrieverForDescriptors(field, content.map { this.analyse(it) }, context)

    /**
     * Performs the [HueHistogram] analysis on the provided [ImageContent] and returns a [FloatVectorDescriptor] that represents the result.
     *
     * @param content [ImageContent] to be analysed.
     * @return [FloatVectorDescriptor] result of the analysis.
     */
    fun analyse(content: ImageContent): FloatVectorDescriptor {
        /* Generate histogram. */
        val hist = FloatArray(VECTOR_SIZE)
        val colors = content.content.getRGBArray()
        for (color in colors) {
            val container: HSVColorContainer = RGBColorContainer(color).toHSV()
            if (container.saturation > 0.2f && container.value > 0.3f) {
                val h: Float = container.hue * hist.size
                val idx = h.toInt()
                hist[idx] += h - idx
                hist[(idx + 1) % hist.size] += idx + 1 - h
            }
        }

        /* Normalize hist. */
        val sum = hist.sum()
        if (sum > 1f) {
            for (i in hist.indices) {
                hist[i] /= sum
            }
        }

        return FloatVectorDescriptor(vector = Value.FloatVector(hist))
    }
}