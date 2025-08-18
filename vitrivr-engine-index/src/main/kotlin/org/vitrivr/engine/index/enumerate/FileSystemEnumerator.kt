package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.file.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [Enumerator] that enumerates a [Path] in a file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.1
 */
class FileSystemEnumerator : OperatorFactory {
    /**
     * Creates a new [Enumerator] instance from this [FileSystemEnumerator].
     *
     * @param name The name of the [Enumerator]
     * @param context The [Context] to use.
     * @return The resulting [Enumerator].
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Enumerator {
        require(inputs.isEmpty()) { "FileSystemEnumerator cannot have any input operators." }
        val parameters = context.local[name] ?: emptyMap()
        val path = Path(parameters["path"] ?: throw IllegalArgumentException("Path is required."))
        val depth = (parameters["depth"] ?: Int.MAX_VALUE.toString()).toInt()
        val skip = parameters["skip"]?.toLongOrNull() ?: 0L
        val limit = parameters["limit"]?.toLongOrNull() ?: Long.MAX_VALUE
        val type = parameters["type"]
        val regex = parameters["regex"]
        val types = parameters["mediaTypes"]?.split(",")?.map { MediaType.valueOf(it) } ?: MediaType.allValid
        logger.debug { "Enumerator: FileSystemEnumerator with path: $path, depth: $depth, mediaTypes: $types, skip: $skip, limit: ${if (limit == Long.MAX_VALUE) "none" else limit} and type: $type, regex: $regex" }
        return if (path.isAbsolute) {
            Instance(path, depth, types, skip, limit, type, regex, name)
        } else {
            Instance(context.schema.root.resolve(path), depth, types, skip, limit, type, regex, name)
        }
    }

    /**
     * The [Enumerator] returned by this [FileSystemEnumerator].
     */
    private class Instance(
        private val path: Path,
        private val depth: Int = Int.MAX_VALUE,
        private val mediaTypes: Collection<MediaType> = MediaType.allValid,
        private val skip: Long = 0,
        private val limit: Long = Long.MAX_VALUE,
        private val typeName: String? = null,
        private val regex: String? = null,
        override val name: String
    ) : Enumerator {

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            logger.debug { "In flow: Start Enumerating with path: $path, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: $limit" }

            val stream = try {
                if (regex == null) {
                    Files.walk(this@Instance.path, this@Instance.depth, FileVisitOption.FOLLOW_LINKS).filter {
                        it.isRegularFile()
                    }.skip(skip).limit(limit)
                } else {
                    Files.walk(this@Instance.path, this@Instance.depth, FileVisitOption.FOLLOW_LINKS).filter {
                        it.isRegularFile() && it.toString().matches(Regex(regex))
                    }.skip(skip).limit(limit)
                }

            } catch (ex: NoSuchFileException) {
                val mes = "In flow: Path ${this@Instance.path} does not exist."
                logger.error { mes }
                scope.coroutineContext.cancel(CancellationException(mes, ex))
                return@flow
            }

            for (element in stream) {
                val type = MimeType.getMimeType(element) ?: continue
                if (type.mediaType in this@Instance.mediaTypes) {
                    val file = try {
                        FileSource(path = element, mimeType = type)
                    } catch (e: FileSystemException) {
                        logger.error(e) { "In flow: Failed to create FileSource for ${element.fileName} (${element.toUri()}). Skip!" }
                        continue
                    }

                    /* Create source ingested and emit it. */
                    val typeName = this@Instance.typeName ?: "SOURCE:${file.type}"
                    emit(
                        Ingested(
                            file.sourceId,
                            typeName,
                            attributes = setOf(SourceAttribute(file)),
                            transient = false
                        )
                    )
                    logger.debug { "In flow: Emitting source ${element.fileName} (${element.toUri()})" }
                }
            }

            /* Emit terminal retrievable to signal, that processing has completed. */
            emit(TerminalRetrievable)
        }.flowOn(Dispatchers.IO)
    }
}
