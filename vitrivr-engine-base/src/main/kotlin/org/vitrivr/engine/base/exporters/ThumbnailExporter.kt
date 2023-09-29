package org.vitrivr.engine.base.exporters

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.content.impl.InMemoryContentFactory
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import java.io.File

class ThumbnailExporter(
        private val location: String = "./thumbnails",
        private val maxSideResolution: Int = 100,
        private val format: String = "jpg",
        override val input: Operator<Ingested>
) : Exporter {

    override fun toFlow(scope: CoroutineScope) : Flow<Ingested> {
        val contentfactory = InMemoryContentFactory()
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent){
                val content = retrievable.deriveContent("MostRepresentativeFrame", contentfactory)
                if (content is ImageContent) {
                    val scaleFactor = if (content.getWidth() > content.getHeight()) {
                        maxSideResolution / content.getWidth()
                    } else {
                        maxSideResolution / content.getHeight()
                    }
                    val thumbnail = content.getContent().getScaledInstance(
                            (content.getWidth() * scaleFactor).toInt(),
                            (content.getHeight() * scaleFactor).toInt(),
                            java.awt.Image.SCALE_SMOOTH
                    )
                    val thumbnailFile = File("$location/${content.hashCode()}.${format}")
                    thumbnailFile.mkdirs()
                    javax.imageio.ImageIO.write(thumbnail as java.awt.image.BufferedImage, format, thumbnailFile)
                }
            }
            retrievable
        }
    }

}