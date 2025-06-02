package org.vitrivr.engine.model3d.decoder

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.model3d.ModelLoader
import java.io.IOException

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Decoder] that can decode [MeshDecoder] from a [Source] of [MediaType.MESH].
 *
 * @author Rahel Arnold
 * @version 1.2.0
 */
class MeshDecoder : OperatorFactory {

    /**
     * Creates and returns a new [Operator] instance from this [OperatorFactory].
     *
     * @param name The name of the [Operator] to create.
     * @param inputs A [Map] of the named input [Operator]s.
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Decoder {
        require(inputs.size == 1) { "MeshDecoder expects a single input. If you want to merge incoming operator do so explicitly." }
        val input = inputs.values.first()
        return Instance(name, input, context)
    }

    /**
     * The [Decoder] returned by this [MeshDecoder].
     */
    private class Instance(
        override val name: String,
        override val input: Operator<out Retrievable>,
        private val context: Context
    ) : Decoder {
        /**
         * Converts this [MeshDecoder] to a [Flow] of [Retrievable] elements.
         *
         * @param scope The [CoroutineScope] used for conversion.
         * @return [Flow] of [Retrievable ]
         */
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> =
            this.input.toFlow(scope).mapNotNull { retrievable ->
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