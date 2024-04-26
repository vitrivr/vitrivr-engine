package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
import java.nio.file.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile

/**
 * An [Enumerator] that enumerates a [Path] in a file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileSystemEnumerator : EnumeratorFactory {

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * Creates a new [Enumerator] instance from this [FileSystemEnumerator].
     *
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(context: IndexContext, parameters: Map<String, String>): Enumerator {
        val path = Path(parameters["path"] ?: throw IllegalArgumentException("Path is required"))
        val depth = (parameters["depth"] ?: Int.MAX_VALUE.toString()).toInt()
        val mediaTypes = (parameters["mediaTypes"] ?: throw IllegalArgumentException("MediaTypes are required"))
            .split(";").map { x ->
                MediaType.valueOf(x.trim())
            }
        val skip = parameters["skip"]?.toLongOrNull() ?: 0L
        val limit = parameters["limit"]?.toLongOrNull() ?: Long.MAX_VALUE
        logger.info { "Enumerator: FileSystemEnumerator with path: $path, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: ${if (limit == Long.MAX_VALUE) "none" else limit}" }
        return Instance(path, depth, mediaTypes, skip, limit)
    }

    /**
     * The [Enumerator] returned by this [FileSystemEnumerator].
     */
    private class Instance(
        private val path: Path,
        private val depth: Int = Int.MAX_VALUE,
        private val mediaTypes: Collection<MediaType> = MediaType.allValid,
        private val skip: Long = 0,
        private val limit: Long = Long.MAX_VALUE
    ) : Enumerator {
        private val logger: KLogger = KotlinLogging.logger {}

        override fun toFlow(scope: CoroutineScope): Flow<Source> = flow {

            logger.debug { "In flow: Start Enumerating with path: $path, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: $limit" }

            val stream = try {
                Files.walk(this@Instance.path, this@Instance.depth, FileVisitOption.FOLLOW_LINKS)
                    .filter { it.isRegularFile() }.skip(skip).limit(limit)
            } catch (ex: NoSuchFileException) {
                val mes = "In flow: Path ${this@Instance.path} does not exist."
                logger.error { mes }
                scope.coroutineContext.cancel(CancellationException(mes, ex))
                //currentCoroutineContext().cancel(CancellationException(mes, ex))
                return@flow
            }
            for (element in stream) {

                val type = MimeType.getMimeType(element) ?: continue
                if (type.mediaType in this@Instance.mediaTypes) {
                    val file = try {
                        FileSource(path = element, mimeType = type)
                    } catch (ex: FileSystemException) {
                        logger.error { "In flow: Failed to create FileSource for ${element.fileName} (${element.toUri()}). Skip!" }
                        continue
                    }
                    emit(file)
                    logger.debug { "In flow: Emitting source ${element.fileName} (${element.toUri()})" }
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}