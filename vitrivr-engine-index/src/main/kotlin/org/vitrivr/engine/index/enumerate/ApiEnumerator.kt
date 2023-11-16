package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.Path

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
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    private val logger: KLogger = KotlinLogging.logger {}

    override fun newOperator(context: IndexContext, parameters: Map<String, String>, inputs: Stream<*>?): Enumerator {
        val paths = inputs as Stream<Path>;
        val depth = (parameters["depth"] ?: Int.MAX_VALUE.toString()).toInt()
        val mediaTypes = (parameters["mediaTypes"] ?: throw IllegalArgumentException("MediaTypes are required"))
            .split(";").map { x ->
                MediaType.valueOf(x.trim())
            }
        val skip = parameters["skip"]?.toLongOrNull() ?: 0L
        val limit = parameters["limit"]?.toLongOrNull() ?: Long.MAX_VALUE
        logger.info { "Enumerator: FileSystemEnumerator with path: $paths, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: ${if (limit == Long.MAX_VALUE) "none" else limit}" }
        return Instance(paths, mediaTypes)
    }

    /**
     * The [Enumerator] returned by this [ApiEnumerator].
     */
    private class Instance(
        private val paths: Stream<Path>,
        private val mediaTypes: Collection<MediaType> = MediaType.allValid,
    ) : Enumerator {
        override fun toFlow(scope: CoroutineScope): Flow<Source> = flow {
            for (element in paths) {
                val type = MimeType.getMimeType(element) ?: continue
                if (type.mediaType in this@Instance.mediaTypes) {
                    emit(FileSource(path = element, mimeType = type))
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}