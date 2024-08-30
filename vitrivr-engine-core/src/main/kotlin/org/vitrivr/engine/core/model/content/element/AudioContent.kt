package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.*

/**
 * A aural [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
interface AudioContent: ContentElement<ShortBuffer> {
    /** Number of samples encoded in this [AudioContent]. */
    val samples: Int
        get() = this.content.limit() / this.channels

    /** The size of this [AudioContent] in bytes. */
    val size: Int
        get() = this.content.limit() * Short.SIZE_BYTES

    /** The number of channels encoded in this [AudioContent]. */
    val channels: Short

    /** The sampling rate of the data encoded in this [AudioContent]. */
    val samplingRate: Int

    /** The [ContentType] of an [AudioContent] is always [ContentType.AUDIO_FRAME]. */
    override val type: ContentType
        get() = ContentType.AUDIO_FRAME

    /**
     * Converts this [AudioContent] to a [ByteBuffer] containing the content in the WAVE format.
     *
     * @return [ByteBuffer] containing the audio in the WAVE format.
     */
    fun toBytes(): ByteBuffer {
        val data = this.content.duplicate()
        val buffer = ByteBuffer.allocate(44 + data.remaining() * 2).order(ByteOrder.LITTLE_ENDIAN)

        // Write WAV header
        val subChunk2Length = data.remaining() * this.channels * (16 / 8) // Assuming 16 bits per sample

        // RIFF Chunk
        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + subChunk2Length)
        buffer.put("WAVE".toByteArray())

        // fmt chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // PCM header size
        buffer.putShort(1) // Audio format 1 = PCM
        buffer.putShort(this.channels)
        buffer.putInt(this.samplingRate)
        buffer.putInt((this.samplingRate * this.channels * (16 / 8))) // Byte rate
        buffer.putShort((this.channels * (16 / 8)).toShort()) // Block align
        buffer.putShort(16) // Bits per sample

        // data chunk
        buffer.put("data".toByteArray())
        buffer.putInt(subChunk2Length)

        while (data.hasRemaining()) {
            val sample = data.get()
            buffer.putShort(sample)
        }
        return buffer.flip()
    }

    /**
     * Converts the audio content to a data URL encoding a WAVE file.
     *
     * @return Data URL
     */
    fun toDataURL(): String {
        val buffer = this.toBytes()
        val base64 = Base64.getEncoder().encodeToString(buffer.array())
        return "data:audio/wav;base64,$base64"
    }
}

