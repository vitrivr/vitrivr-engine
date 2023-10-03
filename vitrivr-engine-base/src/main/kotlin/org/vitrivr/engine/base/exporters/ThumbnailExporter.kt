package org.vitrivr.engine.base.exporters

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.base.resolvers.ImageOnDiskResolver
import org.vitrivr.engine.base.resolvers.ResolvableImage
import org.vitrivr.engine.core.content.impl.InMemoryContentFactory
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.Resolver
import java.awt.image.BufferedImage

class ThumbnailExporter(
        private val maxSideResolution: Int = 100,
        override val input: Operator<Ingested>,
        private val resolver : Resolver = ImageOnDiskResolver(),
) : Exporter {

    override fun toFlow(scope: CoroutineScope) : Flow<Ingested> {
        val contentfactory = InMemoryContentFactory()
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent){
                val content = retrievable.deriveContent("MostRepresentativeFrame", contentfactory)
                if (content is ImageContent) {
                    val scaleFactor = if (content.getWidth() > content.getHeight()) {
                        maxSideResolution.toFloat() / content.getWidth()
                    } else {
                        maxSideResolution.toFloat() / content.getHeight()
                    }
                    val thumbnail = content.getContent().getScaledInstance(
                            (content.getWidth() * scaleFactor).toInt(),
                            (content.getHeight() * scaleFactor).toInt(),
                            java.awt.Image.SCALE_SMOOTH
                    )
                    this.resolver.saveBufferedImage(retrievable.id, thumbnail as BufferedImage)
                }
            }
            retrievable
        }
    }

}