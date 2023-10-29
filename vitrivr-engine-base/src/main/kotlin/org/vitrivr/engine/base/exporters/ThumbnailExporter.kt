package org.vitrivr.engine.base.exporters

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.resolver.Resolver
import org.vitrivr.engine.core.source.file.MimeType

/**
 * An [Exporter] that generates thumbnails from videos and images.
 *
 * @author Finn Faber
 * @version 1.0.0
 */
class ThumbnailExporter(
    private val maxSideResolution: Int,
    override val input: Operator<Ingested>,
    private val resolver : Resolver,
    private val mimeType: MimeType
) : Exporter {
    init {
        require(mimeType in setOf(MimeType.JPEG, MimeType.JPG, MimeType.PNG)) { "ThumbnailExporter only support image formats JPEG and PNG." }
    }

    override fun toFlow(scope: CoroutineScope) : Flow<Ingested> {
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            val resolvable = this.resolver.resolve(retrievable.id)
            if (resolvable != null && retrievable is RetrievableWithContent) {
                val writer = when (mimeType){
                    MimeType.JPEG,
                    MimeType.JPG -> JpegWriter()
                    MimeType.PNG -> PngWriter()
                    else -> throw IllegalArgumentException("Unsupported mime type $mimeType")
                }
                val content = retrievable.content.filterIsInstance<ImageContent>().firstOrNull()
                if (content != null) {
                    val imgBytes = ImmutableImage.fromAwt(content.content).let {
                        if (it.width > it.height){
                            it.scaleToWidth(maxSideResolution)
                        } else {
                            it.scaleToHeight(maxSideResolution)
                        }
                    }.bytes(writer)
                    resolvable.openOutputStream().use {
                        it.write(imgBytes)
                    }
                }
            }
            retrievable
        }
    }
}