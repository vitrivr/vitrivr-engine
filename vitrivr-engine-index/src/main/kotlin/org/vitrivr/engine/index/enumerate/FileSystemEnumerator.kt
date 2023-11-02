package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.slf4j.event.LoggingEvent
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
    /**
     * Creates a new [Enumerator] instance from this [FileSystemEnumerator].
     *
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    private val logger: KLogger = KotlinLogging.logger {}

    override fun newOperator(context: IndexContext, parameters: Map<String, Any>): Enumerator {
        val path = Path(parameters["path"] as String? ?: throw IllegalArgumentException("Path is required"))
        val depth = (parameters["depth"] as String? ?: Int.MAX_VALUE.toString()).toInt()
        val mediaTypes = (parameters["mediaTypes"] as String? ?: throw IllegalArgumentException("MediaTypes are required"))
            .split(";").map { x ->
                MediaType.valueOf(x.trim())
            }
        logger.info { "Enumerator: FileSystemEnumerator with path: $path, depth: $depth, mediaTypes: $mediaTypes" }
        return Instance(path, depth, mediaTypes)
    }

    /**
     * The [Enumerator] returned by this [FileSystemEnumerator].
     */
    private class Instance(private val path: Path, private val depth: Int = Int.MAX_VALUE, private val mediaTypes: Collection<MediaType> = MediaType.allValid) : Enumerator {
        override fun toFlow(scope: CoroutineScope): Flow<Source> = flow {
            val stream = Files.walk(this@Instance.path, this@Instance.depth, FileVisitOption.FOLLOW_LINKS).filter { it.isRegularFile() }
            for (element in stream) {
                val type = MimeType.getMimeType(element) ?: continue
                if (type.mediaType in this@Instance.mediaTypes) {
                    emit(FileSource(path = element, mimeType = type))
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}