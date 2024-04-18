package org.vitrivr.engine.core.model.content.factory

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.model.content.element.*
import org.vitrivr.engine.core.model.content.impl.cache.CachedAudioContent
import org.vitrivr.engine.core.model.content.impl.cache.CachedContent
import org.vitrivr.engine.core.model.content.impl.cache.CachedImageContent
import org.vitrivr.engine.core.model.content.impl.cache.CachedTextContent
import org.vitrivr.engine.core.model.mesh.Model3D
import java.awt.image.BufferedImage
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.nio.ShortBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [ContentFactory] that uses a file cache to back [ContentElement]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CachedContentFactory() : ContentFactory {

    private val basePath: Path = Files.createTempDirectory("vitrivr-cache")

    /** The [ReferenceQueue] used for cleanup. */
    private val referenceQueue = ReferenceQueue<CachedContent>()

    /** Set of all the [PhantomReference]s. */
    private val refSet = HashSet<PhantomReference<CachedContent>>()

    /** Internal counter used. */
    private val counter = AtomicInteger(0)

    init {
        Files.createDirectories(basePath)
        thread(name = "FileCachedContentFactory cleaner thread.", isDaemon = true, start = true) {
            while (true) { //FIXME cleanup mechanism appears not to be working
                try {
                    val reference = this.referenceQueue.poll() as? PhantomReference<out CachedContent>
                    if (reference != null) {
                        this.refSet.remove(reference)
                        val content = reference.get()
                        if (content != null) {
                            try {
                                content.purge()
                                logger.debug { "Purged cached item ${content.path}." }
                            } catch (e: Throwable) {
                                logger.error(e) { "Failed to purge cached item ${content.path}." }
                            }
                        }
                    } else {
                        Thread.sleep(100)
                    }
                } catch (e: InterruptedException) {
                    logger.info { "FileCachedContentFactory cleaner thread interrupted" }
                }
            }
        }
    }

    /**
     * Generates the next [Path] for this [CachedContentFactory].
     *
     * @return Next [Path].
     */
    private fun nextPath(): Path = this.basePath.resolve(this.counter.incrementAndGet().toString())

    override fun newImageContent(bufferedImage: BufferedImage): ImageContent {
        val content = CachedImageContent(this.nextPath(), bufferedImage)
        this.refSet.add(PhantomReference(content, this.referenceQueue))
        return content
    }

    override fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent {
        val content = CachedAudioContent(this.nextPath(), channel, samplingRate, audio)
        this.refSet.add(PhantomReference(content, this.referenceQueue))
        return content
    }

    override fun newTextContent(text: String): TextContent {
        val content = CachedTextContent(this.nextPath(), text)
        this.refSet.add(PhantomReference(content, this.referenceQueue))
        return content
    }

    override fun newMeshContent(model3D: Model3D): Model3DContent = TODO()

}