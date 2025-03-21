package org.vitrivr.engine.module.features.feature.dominantcolor

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.color.RGBColorContainer
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.util.extension.getRGBArray
import java.util.*

class DominantColor : Analyser<ImageContent, LabelDescriptor> {
    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = LabelDescriptor::class

    enum class ColorLabel {
        UNDETERMINED,
        BLACKWHITE,
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        CYAN,
        BLUE,
        VIOLET,
        MAGENTA,
        GRAY
    }

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DominantColor].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) = LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), "")

    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, LabelDescriptor>,
        query: Query,
        context: QueryContext
    ): Retriever<ImageContent, LabelDescriptor> {

        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is BooleanQuery) { "The query is not a BooleanQuery." }

        return DominantColorRetriever(field, query, context)

    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, LabelDescriptor>,
        descriptors: Collection<LabelDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, LabelDescriptor> {

        val labels = descriptors.mapNotNull {
            try {
                ColorLabel.valueOf(it.label.value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }.toSet().map { it.name }

        val query = SimpleBooleanQuery(Value.String(labels.first()), attributeName = "label")

        return newRetrieverForQuery(field, query, context)
    }

    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, LabelDescriptor>,
        content: Collection<ImageContent>,
        context: QueryContext
    ): Retriever<ImageContent, LabelDescriptor> = newRetrieverForDescriptors(field, analyse(content), context)



    override fun newExtractor(
        field: Schema.Field<ImageContent, LabelDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, LabelDescriptor> = DominantColorExtractor(input, this,  field)

    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, LabelDescriptor> = DominantColorExtractor(input, this, name)

    /**
     * Performs the [DominantColor] analysis on the provided [List] of [ImageContent] elements.
     * Adapted from Cineast.
     *
     * @param content The [List] of [ImageContent] elements.
     * @return [List] of [LabelDescriptor]s.
     */
    fun analyse(content: Collection<ImageContent>): List<LabelDescriptor> = content.map {
        val hist = IntArray(10) { 0 }
        val rgb = it.content.getRGBArray()
        rgb.forEach { color ->
            val hsv = RGBColorContainer(color).toHSV()

            if(hsv.saturation < 0.02f){
                ++hist[0]
                return@forEach
            }
            if (hsv.saturation < 0.2f || hsv.value < 0.3f) {
                ++hist[9]
                return@forEach
            } else if (hsv.hue >= 0.07 && hsv.hue < 0.14) {  //orange
                ++hist[2]
            } else if (hsv.hue >= 0.14 && hsv.hue < 0.17) { //yellow
                ++hist[3]
            } else if (hsv.hue >= 0.17 && hsv.hue < 0.44) { //green
                ++hist[4]
            } else if (hsv.hue >= 0.44 && hsv.hue < 0.56) { //cyan
                ++hist[5]
            } else if (hsv.hue >= 0.56 && hsv.hue < 0.73) { //blue
                ++hist[6]
            } else if (hsv.hue >= 0.73 && hsv.hue < 0.76) { //violet
                ++hist[7]
            } else if (hsv.hue >= 0.76 && hsv.hue < 0.92) { //magenta
                ++hist[8]
            } else {
                ++hist[1]
            }

        }

        val max = hist.max()
        val label = if (max < rgb.size / 2) ColorLabel.UNDETERMINED else  ColorLabel.entries[hist.indexOf(max)]

        return@map LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), label.name)
    }
}