package org.vitrivr.engine.model3d.decoder

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.model3d.ModelHandler
import java.io.IOException

/**
 * A [Decoder] that can decode [MeshDecoder] from a [Source] of [MediaType.OBJ] or [MediaType.GLTF].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class MeshDecoder : DecoderFactory {

    /**
     * Creates a new [Decoder] instance from this [Model3DDecoder].
     *
     * @param input The input [Enumerator].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(input: Enumerator, context: IndexContext, parameters: Map<String, String>): Decoder =
        Instance(input, context)

    /**
     * The [Decoder] returned by this [MeshDecoder].
     */
    private class Instance(override val input: Enumerator, private val context: IndexContext) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        /**
         * Converts this [MeshDecoder] to a [Flow] of [Content] elements.
         *
         * Produces [MeshDecoder] elements.
         *
         * @param scope The [CoroutineScope] used for conversion.
         * @return [Flow] of [Content]
         */
        override fun toFlow(scope: CoroutineScope): Flow<Model3DContent> = this.input.toFlow(scope).filter {
            it.type == MediaType.MESH
        }.mapNotNull { source ->
            logger.info { "Decoding source ${source.name} (${source.sourceId})" }
            try {
                val handler = ModelHandler()
                val model = source.newInputStream().use {
                    this.context.contentFactory.newModel3DContent(handler.loadModel(source.sourceId.toString(), source.name.substringAfterLast(".")))
                }
                MeshDecoderWithSource(model, source)
            } catch (e: IOException) {
                logger.error(e) { "Failed to decode 3D model from $source due to an IO exception." }
                null
            }
        }


        /**
         * An internal class that represents a single 3D model associated with a [Source].
         *
         * @see MeshDecoder
         * @see SourcedContent
         */
        class MeshDecoderWithSource(model: Model3DContent, override val source: Source) :
            Model3DContent by model, SourcedContent
    }
}