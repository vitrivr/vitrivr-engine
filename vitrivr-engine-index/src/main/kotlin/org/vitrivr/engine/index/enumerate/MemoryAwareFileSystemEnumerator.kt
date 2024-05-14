package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.file.*
import java.util.stream.Stream
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile

/**
 * An [Enumerator] that enumerates a [Path] in a file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
class MemoryAwareFileSystemEnumerator : EnumeratorFactory {

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * Creates a new [Enumerator] instance from this [MemoryAwareFileSystemEnumerator].
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     */
    override fun newEnumerator(name: String, context: IndexContext, mediaTypes: List<MediaType>): Enumerator {
        val path = Path(context[name, "path"] ?: throw IllegalArgumentException("Path is required."))
        val depth = (context[name, "depth"] ?: Int.MAX_VALUE.toString()).toInt()
        val skip = context[name, "skip"]?.toLongOrNull() ?: 0L
        val limit = context[name, "limit"]?.toLongOrNull() ?: Long.MAX_VALUE
        val type = context[name, "type"]
        logger.info { "Enumerator: FileSystemEnumerator with path: $path, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: ${if (limit == Long.MAX_VALUE) "none" else limit}" }
        return Instance(path, depth, mediaTypes, skip, limit, type)
    }

    /**
     * Creates a new [Enumerator] instance from this [MemoryAwareFileSystemEnumerator].
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     * @param inputs Is ignored.
     */
    override fun newEnumerator(
        name: String,
        context: IndexContext,
        mediaTypes: List<MediaType>,
        inputs: Stream<*>?
    ): Enumerator {
        return newEnumerator(name, context, mediaTypes)
    }

    /**
     * The [Enumerator] returned by this [MemoryAwareFileSystemEnumerator].
     */
    private class Instance(
        private val path: Path,
        private val depth: Int = Int.MAX_VALUE,
        private val mediaTypes: Collection<MediaType> = MediaType.allValid,
        private val skip: Long = 0,
        private val limit: Long = Long.MAX_VALUE,
        private val typeName: String? = null
    ) : Enumerator {

        private val logger: KLogger = KotlinLogging.logger {}

        private var totalMemory: Long = 0L
        private var maxMemory: Long = 0L
        private var freeMemory: Long = 0L
        private var usedMemory: Long = 0L
        private var availableMemory: Long = 0L

        init {
            runBlocking { memoryControlLoopThread() }
        }

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {

            "In flow: Start Enumerating with path: $path, depth: $depth, mediaTypes: $mediaTypes, skip: $skip, limit: $limit"
                .let { logger.debug { it } }


            val stream = try {
                Files.walk(this@Instance.path, this@Instance.depth, FileVisitOption.FOLLOW_LINKS)
                    .filter { it.isRegularFile() }.skip(skip).limit(limit)
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
                    } catch (ex: FileSystemException) {
                        "In flow: Failed to create FileSource for ${element.fileName} (${element.toUri()}). Skip!"
                            .let { logger.error { it } }
                        continue
                    }

                    /* Create source ingested and emit it. */
                    val typeName = this@Instance.typeName ?: "SOURCE:${file.type}"
                    val ingested = Ingested(file.sourceId, typeName, false)
                    ingested.addAttribute(SourceAttribute(file))

                    emit(ingested)
                    "In flow: Emitting source ${element.fileName} (${element.toUri()})"
                        .let { logger.debug { it } }
                }
            }
        }.flowOn(Dispatchers.IO)


        fun memoryControlLoopThread() {
            while (true) {
                measureMemoryUsage()
                Thread.sleep(1000)
            }
        }

        fun measureMemoryUsage() {
            this.totalMemory = Runtime.getRuntime().totalMemory()
            this.maxMemory = Runtime.getRuntime().maxMemory()
            this.freeMemory = Runtime.getRuntime().freeMemory()
            this.usedMemory = totalMemory - freeMemory
            this.availableMemory = maxMemory - usedMemory
            logger.info { "Total Memory: $totalMemory \n Max Memory: $maxMemory \n Free Memory: $freeMemory \n Used Memory: $usedMemory \n Available Memory: $availableMemory" }
        }
    }
}