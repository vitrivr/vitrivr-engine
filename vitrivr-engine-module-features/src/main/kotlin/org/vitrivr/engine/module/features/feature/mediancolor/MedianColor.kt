package org.vitrivr.engine.module.features.feature.mediancolor

import kotlinx.coroutines.flow.merge
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
import org.vitrivr.engine.core.model.color.RGBColorContainer
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.CONTENT_AUTHORS_KEY
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.math.StatisticsHelper.medianFromHistogram
import java.util.*

/**
 * Implementation of the [MedianColor] [Analyser], which derives the median color from an [ImageContent] as [FloatVectorDescriptor].
 *
 * This [Analyser] has little practical relevance these days but acts as a simple example for how to create a custom [Analyser] that uses vectors.
 * Furthermore, it can be used to implement test cases.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class MedianColor : Analyser<ImageContent, FloatVectorDescriptor> {

    companion object {
        private const val VECTOR_SIZE = 3
    }

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [MedianColor].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(VECTOR_SIZE))

    /**
     * Generates and returns a new [MedianColorExtractor] instance for this [MedianColor].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext) = MedianColorExtractor(input, this, context[field.fieldName, CONTENT_AUTHORS_KEY]?.split(",")?.toSet(), field)

    /**
     * Generates and returns a new [MedianColorExtractor] instance for this [MedianColor].
     *
     * @param name The name of the [MedianColorExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ImageContent, FloatVectorDescriptor> = MedianColorExtractor(input, this, context[name, CONTENT_AUTHORS_KEY]?.split(",")?.toSet(), name)

    /**
     * Generates and returns a new [DenseRetriever] instance for this [MedianColor].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: Query, context: QueryContext): DenseRetriever<ImageContent> {
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.FloatVector>, context, LinearCorrespondence(3f))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [MedianColor].
     *
     * Invoking this method involves converting the provided [FloatVectorDescriptor] into a [ProximityQuery] that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): DenseRetriever<ImageContent> {
        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = descriptors.first().vector, k = k, fetchVector = fetchVector), context)
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [MedianColor].
     *
     * Invoking this method involves converting the provided [ImageContent] and the [QueryContext] into a [FloatVectorDescriptor]
     * that can be used to retrieve similar [ImageContent] elements.
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>, context: QueryContext): DenseRetriever<ImageContent> =
        this.newRetrieverForDescriptors(field, content.map { this.analyse(it) }, context)

    /**
     * Performs the [MedianColor] analysis on the provided [List] of [ImageContent] elements.
     *
     * @param content The [List] of [ImageContent] elements.
     * @return [FloatVectorDescriptor]s.
     */
    fun analyse(content: ImageContent): FloatVectorDescriptor {
        val r = IntArray(256)
        val g = IntArray(256)
        val b = IntArray(256)

        /* Extract colors from content and generate histogram. */
        val colors: IntArray = content.content.getRGBArray()
        for (color in colors) {
            val rgb = RGBColorContainer(color)
            r[rgb.redAsInt()]++
            g[rgb.greenAsInt()]++
            b[rgb.blueAsInt()]++
        }

        /* Generate vector from per-color histograms. */
        val lab = RGBColorContainer(medianFromHistogram(r), medianFromHistogram(g), medianFromHistogram(b)).toLab()
        return FloatVectorDescriptor(vector = lab.toVector())
    }
}