package org.vitrivr.engine.base.exporters

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.content.impl.InMemoryContentFactory
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.Resolver
import org.vitrivr.engine.core.source.file.MimeType
import java.awt.image.BufferedImage

class ThumbnailExporter(
        private val maxSideResolution: Int,
        override val input: Operator<Ingested>,
        private val resolver : Resolver,
        private val mimeType: MimeType
) : Exporter {

    override fun toFlow(scope: CoroutineScope) : Flow<Ingested> {
        val contentfactory = InMemoryContentFactory()
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent){
                val writer = when (mimeType){
                    MimeType.JPEG -> JpegWriter()
                    MimeType.JPG -> JpegWriter()
                    MimeType.PNG -> PngWriter()
                    else -> throw IllegalArgumentException("Unsupported mime type $mimeType")
                }
                val content = retrievable.deriveContent("MostRepresentativeFrame", contentfactory)
                if (content is ImageContent) {
                    val img_bytes = ImmutableImage.fromAwt(content.getContent()).let {
                        if (it.width > it.height){
                            it.scaleToWidth(maxSideResolution)
                        }
                        else{
                            it.scaleToHeight(maxSideResolution)
                        }
                    }.bytes(writer)
                    val stream = resolver.getOutputStream(retrievable.id, mimeType)
                    stream.write(img_bytes)
                }
            }
            retrievable
        }
    }

}