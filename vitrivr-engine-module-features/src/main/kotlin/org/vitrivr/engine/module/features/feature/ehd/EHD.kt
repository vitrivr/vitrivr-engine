package org.vitrivr.engine.module.features.feature.ehd

import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
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
import org.vitrivr.engine.core.util.math.MathHelper
import java.util.*
import kotlin.reflect.KClass

/**
 * An MPEG 7 Edge Histogram Descriptor (EHD) [Analyser] for [ImageContent] objects.
 *
 * Migrated from Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class EHD : Analyser<ImageContent, FloatVectorDescriptor> {

    companion object {
        private const val VECTOR_SIZE = 80
        private val MV: FloatArray = floatArrayOf(1f, -1f, 1f, -1f)
        private val MH: FloatArray = floatArrayOf(1f, 1f, -1f, -1f)
        private val M45: FloatArray = floatArrayOf(MathHelper.SQRT2_f, 0f, 0f, -MathHelper.SQRT2_f)
        private val M135: FloatArray = floatArrayOf(0f, MathHelper.SQRT2_f, -MathHelper.SQRT2_f, 0f)
        private val MN: FloatArray = floatArrayOf(2f, -2f, -2f, 2f)

        /**
         *
         */
        private fun edgeType(i1: Int, i2: Int, i3: Int, i4: Int): Int {
            val coeffs = floatArrayOf(
                MV[0] * i1 + MV[1] * i2 + MV[2] * i3 + MV[3] * i4,
                MH[0] * i1 + MH[1] * i2 + MH[2] * i3 + MH[3] * i4,
                M45[0] * i1 + M45[1] * i2 + M45[2] * i3 + M45[3] * i4,
                M135[0] * i1 + M135[1] * i2 + M135[2] * i3 + M135[3] * i4,
                MN[0] * i1 + MN[1] * i2 + MN[2] * i3 + MN[3] * i4,
            )

            var maxid = 0
            for (i in 1..4) {
                if (coeffs[maxid] < coeffs[i]) {
                    maxid = i
                }
            }

            if (coeffs[maxid] >= 14) {
                return maxid
            }

            return -1
        }
    }

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(ImageContent::class)
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [EHD].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): FloatVectorDescriptor = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(VECTOR_SIZE))

    /**
     * Generates and returns a new [EHDExtractor] instance for this [EHD].
     *
     * @param name The name of the [EHDExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = EHDExtractor(input, this, name)

    /**
     * Generates and returns a new [EHDExtractor] instance for this [EHD].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext) = EHDExtractor(input, this,  field)

    /**
     * Generates and returns a new [DenseRetriever] instance for this [EHD].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [DenseRetriever] instance for this [EHD]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: Query, context: QueryContext): DenseRetriever<ImageContent> {
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.FloatVector>, context, LinearCorrespondence(4f))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [EHD].
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
     * Generates and returns a new [DenseRetriever] instance for this [EHD].
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
     * Performs the [EHD] analysis on the provided [ImageContent] and returns a [FloatVectorDescriptor] that represents the result.
     *
     * @param content [ImageContent] to be analysed.
     * @return [FloatVectorDescriptor] result of the analysis.
     */
    fun analyse(content: ImageContent): FloatVectorDescriptor {
        val gray: GrayU8 = ConvertBufferedImage.convertFrom(content.content, null as GrayU8?)
        val width: Int = content.content.width
        val height: Int = content.content.height
        val hist = FloatArray(VECTOR_SIZE)
        for (x in 0..3) {
            for (y in 0..3) {
                val subImage: GrayU8 = gray.subimage(
                    width * x / 4, height * y / 4, width * (x + 1) / 4, height * (y + 1) / 4,
                    null
                )
                var count = 0
                val tmp = IntArray(5)
                var xx = 0
                while (xx < subImage.getWidth() - 1) {
                    var yy = 0
                    while (yy < subImage.getHeight() - 1) {
                        count++
                        val index: Int = edgeType(
                            subImage.unsafe_get(xx, yy),
                            subImage.unsafe_get(xx + 1, yy),
                            subImage.unsafe_get(xx, yy + 1),
                            subImage.unsafe_get(xx + 1, yy + 1)
                        )
                        if (index > -1) {
                            tmp[index]++
                        }
                        yy += 2
                    }
                    xx += 2
                }
                val offset = (4 * x + y) * 5
                for (i in 0..4) {
                    hist[offset + i] += (tmp[i].toFloat()) / count.toFloat()
                }
            }
        }
        return FloatVectorDescriptor(vector = Value.FloatVector(hist))
    }
}