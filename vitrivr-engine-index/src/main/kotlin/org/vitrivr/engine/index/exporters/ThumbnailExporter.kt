package org.vitrivr.engine.index.exporters

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.source.file.MimeType

/**
 * An [Exporter] that generates thumbnails from videos and images.
 *
 * @author Finn Faber
 * @version 1.0.0
 */
class ThumbnailExporter : ExporterFactory {
    /**
     * Creates a new [Exporter] instance from this [ThumbnailExporter].
     *
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(input: Operator<Retrievable>, context: IndexContext, parameters: Map<String, Any>): Exporter {
        val maxSideResolution = parameters["maxSideResolution"] as? Int ?: 100
        val mimeType = parameters["mimeType"] as? MimeType ?: MimeType.JPG
        return Instance(input, context, maxSideResolution, mimeType)
    }

    /**
     * The [Exporter] generated by this [ThumbnailExporter].
     */
    private class Instance(override val input: Operator<Retrievable>, private val context: IndexContext, private val maxResolution: Int, private val mimeType: MimeType) : Exporter {
        init {
            require(mimeType in setOf(MimeType.JPEG, MimeType.JPG, MimeType.PNG)) { "ThumbnailExporter only support image formats JPEG and PNG." }
        }

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { retrievable ->
            val resolvable = this.context.resolver.resolve(retrievable.id)
            if (resolvable != null && retrievable is RetrievableWithContent) {
                val writer = when (mimeType) {
                    MimeType.JPEG,
                    MimeType.JPG -> JpegWriter()

                    MimeType.PNG -> PngWriter()
                    else -> throw IllegalArgumentException("Unsupported mime type $mimeType")
                }
                val content = retrievable.content.filterIsInstance<ImageContent>().firstOrNull()
                if (content != null) {
                    val imgBytes = ImmutableImage.fromAwt(content.content).let {
                        if (it.width > it.height) {
                            it.scaleToWidth(maxResolution)
                        } else {
                            it.scaleToHeight(maxResolution)
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

