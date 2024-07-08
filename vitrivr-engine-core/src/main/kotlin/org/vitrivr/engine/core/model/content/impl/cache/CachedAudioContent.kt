package org.vitrivr.engine.core.model.content.impl.cache

import org.vitrivr.engine.core.model.content.element.AudioContent
import java.lang.ref.SoftReference
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * A [AudioContent] implementation that is backed by a cache file.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CachedAudioContent(override val path: Path, override val channels: Short, override val samplingRate: Int, buffer: ShortBuffer) : AudioContent, CachedContent {
    /** The [SoftReference] of the [ByteBuffer] used for caching. */
    private var reference: SoftReference<ShortBuffer> = SoftReference(buffer)

    /** The number of samples contained in this [CachedAudioContent]. */
    override val samples: Int = buffer.limit() / this.channels

    /** The size of this [CachedAudioContent] in bytes. */
    override val size: Int = buffer.limit() * Short.SIZE_BYTES

    /** The audio samples contained in this [CachedAudioContent]. */
    override val content: ShortBuffer
        get() {
            var buffer = this.reference.get()
            if (buffer == null) {
                buffer = reload()
                this.reference = SoftReference(buffer)
            }
            return buffer.asReadOnlyBuffer()
        }
    override val id: UUID = UUID.randomUUID()

    init {
        val outBuffer = ByteBuffer.allocate(this.size).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until buffer.limit()) {
            outBuffer.putShort(buffer.get(i))
        }
        Files.newByteChannel(this.path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
            it.write(outBuffer.flip())
        }
    }

    /**
     * Reloads the buffered audio sample from disk.
     *
     * @return [ByteBuffer]
     */
    private fun reload(): ShortBuffer {
        val buffer = ByteBuffer.allocate(this.size).order(ByteOrder.LITTLE_ENDIAN)
        Files.newByteChannel(this.path, StandardOpenOption.READ).use { it.read(buffer) }
        return buffer.flip().asShortBuffer()
    }
}