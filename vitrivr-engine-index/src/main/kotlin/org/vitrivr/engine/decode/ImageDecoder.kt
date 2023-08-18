package org.vitrivr.engine.decode

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import java.io.IOException
import javax.imageio.ImageIO

class ImageDecoder(override val enumerator: Enumerator) : Decoder {

    override fun toFlow(): Flow<Content> = flow {
        while (enumerator.hasNext()) {
            val source = enumerator.next()
            if (source.type == MediaType.IMAGE) {
                try {
                    val image = ImageIO.read(source.inputStream)
                    if (image != null) {
                        emit(InMemoryImageContent(source, image))
                    } else {
                        //TODO log
                    }
                } catch (ioException: IOException) {
                    //TODO log
                }
            }
        }
    }.flowOn(Dispatchers.IO)

}