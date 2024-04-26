package org.vitrivr.engine.index.decode

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.io.IOException
import javax.imageio.ImageIO

/**
 * A [Decoder] that can decode [ImageContent] from a [Source] of [MediaType.IMAGE].
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class ImageDecoder : DecoderFactory {

    /**
     * Creates a new [Decoder] instance from this [ImageDecoder].
     *
     * @param name the name of the [Decoder]
     * @param input The input [Enumerator].
     * @param context The [IndexContext] to use.
     */
    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder = Instance(input, context)

    /**
     * The [Decoder] returned by this [ImageDecoder].
     */
    private class Instance(override val input: Enumerator, private val context: IndexContext) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        /**
         * Converts this [ImageDecoder] to a [Flow] of [Content] elements.
         *
         * Produces [ImageContent] elements.
         *
         * @param scope The [CoroutineScope] used for conversion.
         * @return [Flow] of [Content]
         */
        override fun toFlow(scope: CoroutineScope): Flow<Ingested> = this.input.toFlow(scope).filter {
            it.type == MediaType.IMAGE
        }.mapNotNull { source ->
            logger.debug { "In flow: Decoding source ${source.name} (${source.sourceId})" }
            try {
                val content = source.newInputStream().use {
                    this.context.contentFactory.newImageContent(ImageIO.read(it))
                }
                val ingested = Ingested(source.sourceId, source.type.toString(), false)

                /* Append source and content to ingested. */
                ingested.addAttribute(SourceAttribute(source))
                ingested.addContent(content)

                /* Return ingested. */
                ingested
            } catch (e: IOException) {
                logger.error(e) { "Failed to decode image from $source due to an IO exception." }
                null
            }
        }
    }
}
