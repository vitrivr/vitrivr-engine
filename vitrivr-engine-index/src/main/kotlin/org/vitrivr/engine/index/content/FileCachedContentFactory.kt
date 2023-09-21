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
        thread(name = "FileCachedContentFactory cleaner thread", isDaemon = true, start = true) {

            while (true) {
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

    private inner class FileBackedImageContent(bufferedImage: BufferedImage) : ImageContent {

        private val backing: FileBackedByteBuffer
        private val width = bufferedImage.width
        private val height = bufferedImage.height
        private val type = bufferedImage.type

        init {
            val colors = bufferedImage.getRGBArray()
            val buf = ByteBuffer.allocate(colors.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            for (c in colors) {
                buf.putInt(c)
            }
            backing = FileBackedByteBuffer(buf.array(), basePath.resolve(counter.getAndIncrement().toString()))
        }

        override fun getContent(): BufferedImage {
            val colors = backing.buffer.asIntBuffer().array()
            val image = BufferedImage(width, height, type)
            image.setRGBArray(colors)
            return image
        }

    }

    override fun newImageContent(bufferedImage: BufferedImage): ImageContent = FileBackedImageContent(bufferedImage)

    private inner class FileBackedAudioContent(
        override val channel: Int,
        override val samplingRate: Int,
        audio: ShortBuffer
    ) : AudioContent {

        private val backing: FileBackedByteBuffer

        init {
            val samples = audio.array()
            val buf = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in samples) {
                buf.putShort(s)
            }
            backing = FileBackedByteBuffer(buf.array(), basePath.resolve(counter.getAndIncrement().toString()))
        }

        override fun getContent(): ShortBuffer = backing.buffer.asShortBuffer().asReadOnlyBuffer()

    }

    override fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent =
        FileBackedAudioContent(channel, samplingRate, audio)

    private inner class FileBackedTextContent(text: String) : TextContent {

        private val backing: FileBackedByteBuffer =
            FileBackedByteBuffer(text.encodeToByteArray(), basePath.resolve(counter.getAndIncrement().toString()))

        override fun getContent(): String = String(backing.buffer.array())

    }

    override fun newTextContent(text: String): TextContent = FileBackedTextContent(text)
}