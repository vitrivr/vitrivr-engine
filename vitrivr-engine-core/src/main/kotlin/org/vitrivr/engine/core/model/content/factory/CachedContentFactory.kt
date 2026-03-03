package org.vitrivr.engine.core.model.content.factory

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.model.content.element.*
import org.vitrivr.engine.core.model.content.impl.cache.*
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.core.model.metamodel.Schema
import java.awt.image.BufferedImage
import java.io.IOException
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.nio.ShortBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

private val logger: KLogger = KotlinLogging.logger {}

/**
 *  A [ContentFactoriesFactory] that creates a [ContentFactory] data backed by a file-cache
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
class CachedContentFactory : ContentFactoriesFactory {
    /**
     * Creates a new [Instance] instance from this [CachedContentFactory].
     *
     * @param schema The [Schema] to create [CachedContentFactory] for.
     * @param parameters The configuration parameters.
     * @return The resulting [ContentFactory].
     */
    override fun newContentFactory(schema: Schema, parameters: Map<String, String>): ContentFactory {
        val path = parameters["location"]?.let { Path.of(it) } ?: Files.createTempDirectory("vitrivr-cache")
        return if (path.isAbsolute) {
            Instance(path)
        } else {
            Instance(schema.root.resolve(path))
        }
    }

    /**
     * A [ContentFactory] that uses a file cache to back [ContentElement]s.
     *
     * @author Ralph Gasser
     * @version 1.0.1
     */
    private class Instance(private val path: Path) : ContentFactory, AutoCloseable {

        /** The [ReferenceQueue] used for cleanup. */
        private val referenceQueue = ReferenceQueue<CachedContent>()

        /** Set of all the [CachedItem]s. */
        private val refSet = HashSet<CachedItem>()

        /** Internal counter used. */
        private val counter = AtomicLong(0)

        /** Flag indicating, that this [CachedContentFactory] has been closed. */
        @Volatile
        var closed = false
            private set

        init {
            if (!Files.exists(this.path)) {
                Files.createDirectories(this.path)
            }
            thread(name = "FileCachedContentFactory cleaner thread.", isDaemon = true, start = true)
            {
                while (!this@Instance.closed) {
                    try {
                        val reference = this.referenceQueue.poll() as? CachedItem
                        if (reference != null) {
                            reference.purge()
                        } else {
                            Thread.sleep(100)
                        }
                    } catch (e: InterruptedException) {
                        logger.error(e) { "FileCachedContentFactory cleaner thread interrupted" }
                    }
                }

                /* Purge all remaining items. */
                this.refSet.forEach { it.purge() }
            }
        }

        /**
         * Generates the next [Path] for this [CachedContentFactory].
         *
         * @return Next [Path].
         */
        private fun nextPath(): Path = this.path.resolve(this.counter.incrementAndGet().toString())

        override fun newImageContent(bufferedImage: BufferedImage): ImageContent {
            check(!this.closed) { "CachedContentFactory has been closed." }
            val content = CachedImageContent(this.nextPath(), bufferedImage)
            this.refSet.add(CachedItem(content, this.referenceQueue))
            return content
        }

        override fun newAudioContent(channels: Short, sampleRate: Int, audio: ShortBuffer): AudioContent {
            check(!this.closed) { "CachedContentFactory has been closed." }
            val content = CachedAudioContent(this.nextPath(), channels, sampleRate, audio)
            this.refSet.add(CachedItem(content, this.referenceQueue))
            return content
        }

        override fun newTextContent(text: String): TextContent {
            check(!this.closed) { "CachedContentFactory has been closed." }
            val content = CachedTextContent(this.nextPath(), text)
            this.refSet.add(CachedItem(content, this.referenceQueue))
            return content
        }

        override fun newMeshContent(model3d: Model3d): Model3dContent {
            check(!this.closed) { "CachedContentFactory has been closed." }
            val content = CachedModel3dContent(this.nextPath(), model3d)
            this.refSet.add(CachedItem(content, this.referenceQueue))
            return content
        }

        /**
         * A [PhantomReference] implementation that allows for the puring of [CachedContent] from the cache.
         */
        inner class CachedItem(referent: CachedContent, queue: ReferenceQueue<in CachedContent>) :
            PhantomReference<CachedContent>(referent, queue) {
            /** The cache path. */
            val path = referent.path

            /** Purges the content from the cache by deleting the file. */
            fun purge() = try {
                Files.deleteIfExists(this.path)
            } catch (e: IOException) {
                logger.error(e) { "Failed to purge cached item $path." }
            } finally {
                this@Instance.refSet.remove(this)
                logger.debug { "Purged cached item $path." }
            }
        }

        /**
         * Closes this [CachedContentFactory] and purges all remaining items. A closed [CachedContentFactory] cannot be used anymore.
         */
        override fun close() {
            this.closed = true
        }
    }
}
