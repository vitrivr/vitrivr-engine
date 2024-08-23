package org.vitrivr.engine.module.features.feature.cld

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
import org.vitrivr.engine.core.model.color.ColorUtilities
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
import org.vitrivr.engine.core.util.math.MathHelper.SQRT1_2
import java.util.*
import kotlin.math.cos
import kotlin.math.floor
import kotlin.reflect.KClass

/**
 * An MPEG 7 Color Layout (CLD) [Analyser] for [ImageContent] objects.
 *
 * Migrated from Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CLD : Analyser<ImageContent, FloatVectorDescriptor> {
    companion object {
        private const val VECTOR_SIZE = 12


        private val SCAN: IntArray = intArrayOf(
            0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32,
            25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7,
            14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37,
            44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62,
            63
        )
    }

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(ImageContent::class)
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLD].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): FloatVectorDescriptor = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(VECTOR_SIZE))

    /**
     * Generates and returns a new [CLDExtractor] instance for this [CLD].
     *
     * @param name The name of the [CLDExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = CLDExtractor(input, this, name)

    /**
     * Generates and returns a new [CLDExtractor] instance for this [CLD].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext) = CLDExtractor(input, this, field)

    /**
     * Generates and returns a new [DenseRetriever] instance for this [CLD].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [DenseRetriever] instance for this [CLD]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, FloatVectorDescriptor>, query: Query, context: QueryContext): DenseRetriever<ImageContent> {
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.FloatVector>, context, LinearCorrespondence(490f))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [CLD].
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
     * Generates and returns a new [DenseRetriever] instance for this [CLD].
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
     * Performs the [CLD] analysis on the provided [ImageContent] and returns a [FloatVectorDescriptor] that represents the result.
     *
     * @param content [ImageContent] to be analysed.
     * @return [FloatVectorDescriptor] result of the analysis.
     */
    fun analyse(content: ImageContent): FloatVectorDescriptor {
        val tmpList: ArrayList<Int> = ArrayList<Int>(content.content.width * content.content.height)

        /* Extract and normalize colors based on alpha channel. */
        val colors: IntArray = content.content.getRGBArray()
        for (c in colors) {
            val rgb = RGBColorContainer(c)
            if (rgb.alpha < 127) {
                tmpList.add(255)
            } else {
                tmpList.add(c)
            }
        }

        /* Create partitions. */
        val partitions: ArrayList<LinkedList<Int>> = partition(tmpList, content.content.width, content.content.height)
        val rgbs = IntArray(64)
        for (i in partitions.indices) {
            rgbs[i] = ColorUtilities.avg(partitions[i].map { RGBColorContainer(it) }).toRGBInt()
        }

        /* Obtain YCbCr values and calculate DCT. */
        val ycbcrs = Array(3) { FloatArray(64) }
        for (i in 0..63) {
            val c = RGBColorContainer(rgbs[i]).toYCbCr()
            ycbcrs[0][i] = c.y
            ycbcrs[1][i] = c.cb
            ycbcrs[2][i] = c.cr
        }
        ycbcrs[0] = this.dct(ycbcrs[0])
        ycbcrs[1] = this.dct(ycbcrs[1])
        ycbcrs[2] = this.dct(ycbcrs[2])

        /* Obtain CLD. */
        val cld = Value.FloatVector(
            floatArrayOf(
                ycbcrs[0][0].toFloat(), ycbcrs[0][1].toFloat(), ycbcrs[0][2].toFloat(), ycbcrs[0][3].toFloat(), ycbcrs[0][4].toFloat(), ycbcrs[0][5].toFloat(),
                ycbcrs[1][0].toFloat(), ycbcrs[1][1].toFloat(), ycbcrs[1][2].toFloat(),
                ycbcrs[2][0].toFloat(), ycbcrs[2][1].toFloat(), ycbcrs[2][2].toFloat()
            )
        )
        return FloatVectorDescriptor(vector = cld)
    }

    /**
     * Calculates the Discrete Cosine Transform (DCT) of a given block.
     *
     * Based on c implementation by Berk ATABEK (http://www.batabek.com/)
     *
     * @param block Block for which to calculate the DCT.
     * @return The DCT of the block.
     */
    private fun dct(block: FloatArray): FloatArray {
        var sum: Double
        var cu: Double
        var cv: Double
        val temp = FloatArray(64)

        for (u in 0..7) {
            for (v in 0..7) {
                sum = 0.0
                cu = if ((u == 0)) SQRT1_2 else 1.0
                cv = if ((v == 0)) SQRT1_2 else 1.0
                for (x in 0..7) {
                    for (y in 0..7) {
                        sum += (block[x * 8 + y] * cos((2 * x + 1) * u * Math.PI / 16.0) * cos((2 * y + 1) * v * Math.PI / 16.0))
                    }
                }
                temp[SCAN[8 * u + v]] = floor((0.25 * cu * cv * sum) + 0.5).toFloat()
            }
        }
        return temp
    }

    /**
     * Generates 8 x 8 partitions of the input list.
     *
     * @param input List to partition.
     * @param width Width of the image.
     * @param height Height of the image.
     * @return List of 8 x 8 partitions.
     */
    private fun <T> partition(input: List<T>, width: Int, height: Int): ArrayList<LinkedList<T>> {
        val ret = ArrayList<LinkedList<T>>(8 * 8)
        for (i in 0 until 8 * 8) {
            ret.add(LinkedList())
        }

        for ((i, t) in input.withIndex()) {
            val index = (((i % width) * 8) / width) + 8 * (i * 8 / width / height)
            ret[index].add(t)
        }

        return ret
    }
}