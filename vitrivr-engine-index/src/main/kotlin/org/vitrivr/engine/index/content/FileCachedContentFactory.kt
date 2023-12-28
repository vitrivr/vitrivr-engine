package org.vitrivr.engine.index.content

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.extension.setRGBArray
import java.awt.image.BufferedImage
import java.io.IOException
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes

class FileCachedContentFactory(private val basePath: Path) : ContentFactory {

    private val logger: KLogger = KotlinLogging.logger {}

    private val referenceQueue = ReferenceQueue<FileBackedByteBuffer>()

    private val counter = AtomicInteger(0)

    init {

        Files.createDirectories(basePath)

        thread(name = "FileCachedContentFactory cleaner thread", isDaemon = true, start = true) {

            while (true) { //FIXME cleanup mechanism appears not to be working
                try {
                    val reference = referenceQueue.remove()
                    reference.get()?.cleanup()
                } catch (e: InterruptedException) {
                    logger.info { "FileCachedContentFactory cleaner thread interrupted" }
                }
            }

        }
    }

    private inner class FileBackedByteBuffer(data: ByteArray, private val path: Path) {

        private var reference: SoftReference<ByteBuffer>

        init {
            path.outputStream(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
                .write(data)
            reference = SoftReference(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN))
            PhantomReference(this, referenceQueue)
        }

        @get:Synchronized
        val buffer: ByteBuffer
            get() {
                val buffer = reference.get()
                if (buffer != null) {
                    return buffer
                }
                return try {
                    val data = path.readBytes()
                    val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
                    reference = SoftReference(buf)
                    buf
                } catch (e: IOException) {
                    logger.error(e) { "Could not load FileBackedByteBuffer from '$path'" }
                    ByteBuffer.wrap(ByteArray(0)).order(ByteOrder.LITTLE_ENDIAN)
                }
            }

        fun cleanup() {
            reference.clear()
            val deleted = path.deleteIfExists()
            logger.trace { "cleaning up $path, success: $deleted" }
        }

    }

    /**
     * This class represents a [ImageContent] that is backed by a file.
     */
    private inner class FileBackedImageContent(bufferedImage: BufferedImage) : ImageContent {

        /** The [FileBackedByteBuffer] used for caching. */
        private val backing: FileBackedByteBuffer

        /** The type of the [BufferedImage]. */
        private val imageType = bufferedImage.type

        /** The width of the [BufferedImage] (is stored explicitly). */
        override val width = bufferedImage.width

        /** The height of the [BufferedImage] (is stored explicitly). */
        override val height = bufferedImage.height

        /** This method reads the [BufferedImage] directly from the file system. */
        override val content: BufferedImage
            get() {
                val buf = this.backing.buffer.asIntBuffer()
                val colors = IntArray(buf.remaining())
                buf.get(colors)
                val image = BufferedImage(width, height, imageType)
                image.setRGBArray(colors)
                return image
            }

        init {
            val colors = bufferedImage.getRGBArray()
            val buf = ByteBuffer.allocate(colors.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            for (c in colors) {
                buf.putInt(c)
            }
            this.backing = FileBackedByteBuffer(buf.array(), basePath.resolve(counter.getAndIncrement().toString()))
        }
    }

    override fun newImageContent(bufferedImage: BufferedImage): ImageContent = FileBackedImageContent(bufferedImage)

    /**
     * This class represents a [AudioContent] that is backed by a file.
     */
    private inner class FileBackedAudioContent(override val channel: Int, override val samplingRate: Int, audio: ShortBuffer) : AudioContent {

        private val backing: FileBackedByteBuffer
        override val content: ShortBuffer
            get() = this.backing.buffer.asShortBuffer().asReadOnlyBuffer()

        init {
            val samples = audio.array()
            val buf = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in samples) {
                buf.putShort(s)
            }
            backing = FileBackedByteBuffer(buf.array(), basePath.resolve(counter.getAndIncrement().toString()))
        }
    }

    override fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent =
        FileBackedAudioContent(channel, samplingRate, audio)

    /**
     * This class represents a [TextContent] that is backed by a file.
     */
    private inner class FileBackedTextContent(text: String) : TextContent {

        private val backing: FileBackedByteBuffer = FileBackedByteBuffer(text.encodeToByteArray(), basePath.resolve(counter.getAndIncrement().toString()))

        /** The length of the text is stored explicitly.  */
        override val length: Int = text.length

        /** This method reads the [String] directly from the file system. */
        override val content: String
            get() {
                val buf = backing.buffer
                val arr = ByteArray(buf.remaining())
                buf.get(arr)
                return String(arr)
            }
    }

    override fun newTextContent(text: String): TextContent = FileBackedTextContent(text)
}