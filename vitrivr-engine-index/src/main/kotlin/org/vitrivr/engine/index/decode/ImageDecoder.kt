package org.vitrivr.engine.index.decode

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.io.IOException
import javax.imageio.ImageIO

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Decoder] that can decode [ImageContent] from a [Source] of [MediaType.IMAGE].
 *
 * @author Luca Rossetto
 * @version 1.1.0
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
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).mapNotNull { sourceRetrievable ->
            val source = sourceRetrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: return@mapNotNull null
            if (source.type != MediaType.IMAGE) {
                logger.debug { "In flow: Skipping source ${source.name} (${source.sourceId}) because it is not of type IMAGE." }
                return@mapNotNull null
            }
            logger.debug { "In flow: Decoding source ${source.name} (${source.sourceId})" }
            try {
                val content = source.newInputStream().use {
                    this.context.contentFactory.newImageContent(ImageIO.read(it))
                }
                sourceRetrievable.addContent(content)
                logger.info { "Finished decoding image from source '${source.name}' (${source.sourceId})." }

                /* Return ingested. */
                sourceRetrievable
            } catch (e: IOException) {
                logger.error(e) { "Failed to decode image from source '${source.name}' (${source.sourceId})." }
                null
            }
        }
    }
}
