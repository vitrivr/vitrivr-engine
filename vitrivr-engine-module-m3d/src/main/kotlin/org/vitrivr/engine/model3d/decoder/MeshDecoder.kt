package org.vitrivr.engine.model3d.decoder

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionStatus
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.model3d.ModelLoader
import java.io.IOException

/**
 * A [Decoder] that can decode [MeshDecoder] from a [Source] of [MediaType.MESH].
 *
 * @author Rahel Arnold
 * @version 1.1.2
 */
class MeshDecoder : DecoderFactory {
    /**
     * Creates a new [Decoder] instance from this [DecoderFactory].
     *
     * @param name The name of this [Decoder].
     * @param input The input [Enumerator].
     * @param context The [IndexContext]
     */
    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder = Instance(input, context, name)
    /**
     * The [Decoder] returned by this [MeshDecoder].
     */
    private class Instance(override val input: Enumerator, private val context: IndexContext, override val name: String) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        /**
         * Converts this [MeshDecoder] to a [Flow] of [Retrievable] elements.
         *
         * @param scope The [CoroutineScope] used for conversion.
         * @return [Flow] of [Retrievable ]
         */
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).mapNotNull { retrievable ->
            val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
            if (source?.type != MediaType.MESH) {
                logger.debug { "Skipping retrievable ${retrievable.id} because it is not of type MESH." }
                return@mapNotNull retrievable
            }

            logger.info { "Decoding source ${source.name} (${source.sourceId})" }

            try {
                val handler = ModelLoader()
                val model = source.newInputStream().use {
                    handler.loadModel(source.sourceId.toString(), it) // Pass InputStream directly
                }
                val modelContent = this.context.contentFactory.newMeshContent(model!!)
                logger.info { "Model decoded successfully for source ${source.name} (${source.sourceId})" }
                retrievable.copy(content = retrievable.content + modelContent)
            } catch (e: IOException) {
                logger.error(e) { "Failed to decode 3D model from $source due to an IO exception." }
                throw e
            } catch (e: Throwable) {
                logger.error(e) { "Failed to decode 3D model from source '${source.name}' (${source.sourceId}) due to exception: ${e.message}" }
                throw e
            }
        }
    }
}