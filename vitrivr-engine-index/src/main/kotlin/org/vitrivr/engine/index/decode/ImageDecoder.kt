package org.vitrivr.engine.index.decode

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.io.IOException
import javax.imageio.ImageIO

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Decoder] that can decode [ImageContent] from a [Source] of [MediaType.IMAGE].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.3.0
 */
class ImageDecoder : OperatorFactory {
    /**
     * Creates a new [Instance] instance from this [ImageDecoder.Instance].
     *
     * @param name the name of the [ImageDecoder.Instance]
     * @param inputs Map of named input [Operator]s
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
       require(inputs.size == 1)  { "The ${this::class.simpleName} only supports one input operator. If you want to combine multiple inputs, use explicit merge strategies." }
       return Instance(name, inputs.values.first(), context)
    }

    /**
     * The [Decoder] returned by this [ImageDecoder].
     */
    private class Instance(
        override val name: String,
        override val input: Operator<out Retrievable>,
        private val context: Context,
    ) : Decoder {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).mapNotNull { retrievable ->
            val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
            if (source?.type != MediaType.IMAGE) {
                logger.debug { "Skipping retrievable ${retrievable.id} because it is not of type IMAGE." }
                return@mapNotNull retrievable
            }
            logger.debug { "Decoding source ${source.name} (${source.sourceId})" }
            try {
                val content = source.newInputStream().use {
                    val image = ImageIO.read(it)
                    if (image == null) {
                        logger.warn { "Failed to decode image from source '${source.name}' (${source.sourceId})." }
                        return@mapNotNull null
                    }
                    this.context.contentFactory.newImageContent(image)
                }
                logger.info { "Finished decoding image from source '${source.name}' (${source.sourceId})." }

                /* Return ingested. */
                retrievable.copy(content = retrievable.content + content, attributes = retrievable.attributes)
            } catch (e: IOException) {
                logger.error(e) { "Failed to decode image from source '${source.name}' (${source.sourceId}) due to IO exception: ${e.message}" }
                null
            } catch (e: Throwable) {
                logger.error(e) { "Failed to decode image from source '${source.name}' (${source.sourceId}) due to exception: ${e.message}" }
                null
            }
        }.flowOn(Dispatchers.IO)
    }
}
