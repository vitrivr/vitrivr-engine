package org.vitrivr.base.engine.decode

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

/**
 * A [Decoder] that can decode [BufferedImage]s from a [Source] of [MediaType.IMAGE].
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class ImageDecoder(override val input: Operator<Source>) : Decoder {
    override fun toFlow(): Flow<Content> = this.input.toFlow().filter { it.type == MediaType.IMAGE }.mapNotNull { source ->
        try {
            val image: BufferedImage = ImageIO.read(source.inputStream)
            InMemoryImageContent(source, image)
        } catch (ioException: IOException) {
            //TODO log
            null
        }
    }.flowOn(Dispatchers.IO)
}