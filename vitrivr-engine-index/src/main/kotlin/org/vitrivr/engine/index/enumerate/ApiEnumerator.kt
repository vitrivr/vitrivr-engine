package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.file.FileSystemException
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.Path

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [Enumerator] that enumerates a [Path] in a file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @author Raphael Waltenspül
 * @version 1.0.0
 */
class ApiEnumerator : EnumeratorFactory {
    /**
     * Creates a new [Enumerator] instance from this [ApiEnumerator].
     *
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     */
    @Suppress("UNCHECKED_CAST")
    override fun newOperator(name: String, context: IndexContext, mediaTypes: List<MediaType>, inputs: Stream<*>?): Enumerator {
        val paths = inputs as Stream<Path>
        val depth = (context[name, "depth"] ?: Int.MAX_VALUE.toString()).toInt()
        val skip = context[name, "skip"]?.toLongOrNull() ?: 0L
        val limit = context[name, "limit"]?.toLongOrNull() ?: Long.MAX_VALUE
        val typeName = context[name, "type"]
        logger.info { "Enumerator: FileSystemEnumerator with path: $paths, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: ${if (limit == Long.MAX_VALUE) "none" else limit}" }
        return Instance(paths, mediaTypes, typeName)
    }

    /**
     * The [Enumerator] returned by this [ApiEnumerator].
     */
    private class Instance(
        private val paths: Stream<Path>,
        private val mediaTypes: Collection<MediaType> = MediaType.allValid,
        private val typeName: String? = null
    ) : Enumerator {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            for (element in paths) {
                val type = MimeType.getMimeType(element) ?: continue
                if (type.mediaType in this@Instance.mediaTypes) {
                    /* Create file source. */
                    val file = try {
                        FileSource(path = element, mimeType = type)
                    } catch (ex: FileSystemException) {
                        logger.error { "In flow: Failed to create FileSource for ${element.fileName} (${element.toUri()}). Skip!" }
                        continue
                    }

                    /* Create source ingested and emit it. */
                    val typeName = this@Instance.typeName ?: "SOURCE:${file.type}"
                    val ingested = Ingested(file.sourceId, typeName, false)
                    ingested.addAttribute(SourceAttribute(file))
                    emit(ingested)
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}
