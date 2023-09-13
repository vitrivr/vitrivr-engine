package org.vitrivr.engine.index.decode

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.AudioContent
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.content.SourcedContent
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Decoder] that can decode [ImageContent] from a [Source] of [MediaType.IMAGE].
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class ImageDecoder(override val input: Operator<Source>) : Decoder {

    /**
     * Converts this [ImageDecoder] to a [Flow] of [Content] elements.
     *
     * Produces [ImageContent] elements.
     *
     * @param scope The [CoroutineScope] used for conversion.
     * @return [Flow] of [Content]
     */
    override fun toFlow(scope: CoroutineScope): Flow<ImageContent> = this.input.toFlow(scope).filter {
        it.type == MediaType.IMAGE
    }.mapNotNull { source ->
        try {
            val image = InMemoryImageContent(ImageIO.read(source.inputStream)) /* TODO: Derive using factory. */
            ImageContentWithSource(image, source)
        } catch (e: IOException) {
            logger.error(e) { "Failed to decode image from $source due to an IO exception." }
            null
        }
    }

    /**
     * An internal class that represents a single image associated with a [Source].
     *
     * @see ImageContent
     * @see SourcedContent.Temporal
     */
    class ImageContentWithSource(image: ImageContent, override val source: Source): ImageContent by image, SourcedContent<BufferedImage>
}