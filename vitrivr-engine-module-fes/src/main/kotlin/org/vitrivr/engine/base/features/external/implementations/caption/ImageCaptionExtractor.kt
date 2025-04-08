package org.vitrivr.engine.base.features.external.implementations.caption

import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.base.features.external.api.ConditionalImageCaptioningApi
import org.vitrivr.engine.base.features.external.api.ImageCaptioningApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.caption.ImageCaption.Companion.PROMPT_PARAMETER_NAME
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

val logger = KotlinLogging.logger {}

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class ImageCaptionExtractor : FesExtractor<ImageContent, TextDescriptor> {

    constructor(
        input: Operator<Retrievable>,
        field: Schema.Field<ImageContent, TextDescriptor>,
        analyser: ExternalFesAnalyser<ImageContent, TextDescriptor>,
        parameters: Map<String, String>
    ) : super(input, field, analyser, parameters)

    constructor(
        input: Operator<Retrievable>,
        name: String,
        analyser: ExternalFesAnalyser<ImageContent, TextDescriptor>,
        parameters: Map<String, String>
    ) : super(input, name, analyser, parameters)


    /** The [ImageCaptioningApi] used to perform extraction with. */
    private val captioningApi by lazy { ImageCaptioningApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries) }

    /** The [ConditionalImageCaptioningApi] used to perform extraction with. */
    private val conditionalCaptioningApi by lazy { ConditionalImageCaptioningApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries) }

    private fun makeCaption(imageContent: List<ImageContent>, text: List<String?>) : List<TextDescriptor> {
        val withTextIndices = text.mapIndexedNotNull { index, t -> if (t != null) index to t else null }
        val withoutTextIndices = text.mapIndexedNotNull { index, t -> if (t == null) index else null }


        val withTextResults = if (withTextIndices.isEmpty()) {
            emptyList()
        } else {
            this.conditionalCaptioningApi.analyseBatched(withTextIndices.map { imageContent[it.first] to InMemoryTextContent(it.second) })
        }
        val withoutTextResults = if (withoutTextIndices.isEmpty()) {
            emptyList()
        } else {
            this.captioningApi.analyseBatched(withoutTextIndices.map { imageContent[it] })
        }

        // merge results so they are in the same order as the input
        val results = mutableListOf<TextDescriptor>()
        var withTextIndex = 0
        var withoutTextIndex = 0
        for (i in text.indices) {
            if (text[i] != null) {
                results.add(TextDescriptor(UUID.randomUUID(),null,withTextResults[withTextIndex++],this.field))
            } else {
                results.add(TextDescriptor(UUID.randomUUID(),null,withoutTextResults[withoutTextIndex++]))
            }
        }
        return results
    }

    override fun extract(retrievables: List<Retrievable>): List<List<TextDescriptor>> {

        val content = retrievables.map { it.content }
        val imageContents = content.map { it.filterIsInstance<ImageContent>() }

        val texts : List<List<String?>> = content.map { it.filterIsInstance<TextContent>().map { it.content } }.mapIndexed { index, text -> if (text.isEmpty()) {
            List(imageContents[index].size) { this.parameters[PROMPT_PARAMETER_NAME] }
            } else {
                if (text.size != 1) {
                    logger.warn { "Text content has more than one element. Only the first element will be used as an image captioning prompt." }
                }
                List(imageContents[index].size) { text.first() }
            }
        }

        val flatResults = makeCaption(imageContents.flatten(), texts.flatten())

        var index = 0

        return retrievables.map { retrievable ->
            retrievables.map { it.content }.mapNotNull {
                if (it !is ImageContent) {
                    null
                } else {
                    flatResults[index++].let { TextDescriptor(it.id, retrievable.id, it.value, it.field) }
                }
            }
        }
    }
}