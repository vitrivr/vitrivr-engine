package org.vitrivr.engine.index.enumerate

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
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
import java.nio.file.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
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
@Deprecated(message = "Use regular FileSystemEnumerator instead")
class MemoryControlledFileSystemEnumerator : EnumeratorFactory {

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * Creates a new [Enumerator] instance from this [MemoryControlledFileSystemEnumerator].
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
        return Instance(path, depth, mediaTypes, skip, limit, type, name)
    }

    /**
     * Creates a new [Enumerator] instance from this [MemoryControlledFileSystemEnumerator].
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
     * The [Enumerator] returned by this [MemoryControlledFileSystemEnumerator].
     */
    private class Instance(
        private val path: Path,
        private val depth: Int = Int.MAX_VALUE,
        private val mediaTypes: Collection<MediaType> = MediaType.allValid,
        private val skip: Long = 0,
        private val limit: Long = Long.MAX_VALUE,
        private val typeName: String? = null,
        override val name: String
    ) : Enumerator {

        private val logger: KLogger = KotlinLogging.logger {}

        private var totalMemory: Long = 0L
        private var maxMemory: Long = 0L
        private var freeMemory: Long = 0L
        private var usedMemory: Long = 0L
        private var availableMemory: Long = 0L

        private val lock = ReentrantLock()
        private val emitCondition = lock.newCondition()

        //private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        val memoryTheread = Thread { memoryControlLoopThread() }
        private val notifier: BlockingQueue<Int> = LinkedBlockingQueue(1)


        init {
            memoryTheread.start()
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

                    this@Instance.lock.lock()
                    try {
                        val n = notifier.take()

                        emit(ingested)
                        "In flow: Emitting source ${element.fileName} (${element.toUri()})"
                            .let { logger.debug { it } }

                    } finally {
                        lock.unlock()
                    }

                }
            }
        }.flowOn(Dispatchers.IO)


        fun memoryControlLoopThread() {
            while (true) {
                Thread.sleep(5000)
                val values = measureMemoryUsage()
                // emit as long as available memory is above 30% of max memory
                if (values["availableMemory"]!! > values["maxMemory"]!! * 0.3) {
                    notifier.takeIf { it.isEmpty() }?.put(1)
                }
            }
        }

        fun measureMemoryUsage(): Map<String, Long> {
            totalMemory = Runtime.getRuntime().totalMemory()
            maxMemory = Runtime.getRuntime().maxMemory()
            freeMemory = Runtime.getRuntime().freeMemory()
            usedMemory = totalMemory - freeMemory
            availableMemory = maxMemory - usedMemory

            "Total Memory:  \n $totalMemory \n Max Memory: $maxMemory \n Free Memory: $freeMemory \n Used Memory: $usedMemory \n Available Memory: $availableMemory  \n Avialable is ${freeMemory.toFloat()/maxMemory.toFloat()} of max memory"
                .let { logger.info { it } }

            return mapOf(
                "totalMemory" to totalMemory,
                "maxMemory" to maxMemory,
                "freeMemory" to freeMemory,
                "usedMemory" to usedMemory,
                "availableMemory" to availableMemory
            )
        }
    }
}