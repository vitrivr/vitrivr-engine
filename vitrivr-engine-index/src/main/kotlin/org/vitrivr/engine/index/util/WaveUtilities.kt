package org.vitrivr.engine.index.util

import org.vitrivr.engine.core.model.content.element.AudioContent
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A collection of utilities for handling WAVE files.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
object WaveUtilities {
    /**
     * Exports a list of [AudioContent] as WAVE file (.wav).
     *
     * @param content List of [AudioContent] to export.
     * @param path The path to the file.
     *
     */
    fun export(content: List<AudioContent>, path: Path) = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
        export(content, it)
    }

    /**
     * Exports a list of [AudioContent] as WAVE file (.wav).
     *
     * @param content List of [AudioContent] to export.
     * @param stream The [OutputStream] to write to.
     *
     */
    fun export(content: List<AudioContent>, stream: OutputStream) {
        if (content.isEmpty()) return

        /* Write samples. */
        val samples = ByteArrayOutputStream()
        var bytes = 0
        content.forEach { audio ->
            val buffer = ByteBuffer.allocate(audio.size).order(ByteOrder.LITTLE_ENDIAN)
            buffer.asShortBuffer().put(audio.content)
            samples.write(buffer.array())
            bytes += buffer.array().size
        }

        /* Write header. */
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        writeWaveHeader(header, content.first().channels, content.first().samplingRate, bytes)

        stream.write(header.array())
        samples.writeTo(stream)
    }

    /**
     * Exports a single [AudioContent] as WAVE file (.wav).
     *
     * @param content [AudioContent] to export.
     * @param path The path to the file.
     */
    fun export(content: AudioContent, path: Path) = export(listOf(content), path)

    /**
     * Exports a single [AudioContent] as WAVE file (.wav).
     *
     * @param content [AudioContent] to export.
     * @param stream The [OutputStream] to export to
     */
    fun export(content: AudioContent, stream: OutputStream) = export(listOf(content), stream)

    /**
     * Writes the WAV header to the ByteBuffer.
     *
     * @param buffer       The ByteBuffer to write the header to.
     * @param channels     The number of channels in the WAV file.
     * @param sampleRate   Sample rate of the output file.
     * @param length       Length in bytes of the frames data
     */
    private fun writeWaveHeader(buffer: ByteBuffer, channels: Short, sampleRate: Int, length: Int) {
        /* Length of the subChunk2. */
        val subChunk2Length: Int = length * channels * Short.SIZE_BYTES  /* Number of bytes for audio data: NumSamples * NumChannels * BytesPerSample. */

        /* RIFF Chunk. */
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + subChunk2Length)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII)) /* WAV format. */

        /* Format chunk. */
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII)) /* Begin of the format chunk. */
        buffer.putInt(16) /* Length of the Format chunk. */
        buffer.putShort(1.toShort()) /* Format: 1 = Raw PCM (linear quantization). */
        buffer.putShort(channels) /* Number of channels. */
        buffer.putInt(sampleRate) /* sampleRate. */
        buffer.putInt((sampleRate * channels * Short.SIZE_BYTES)) /* Byte rate: SampleRate * NumChannels * BytesPerSample */
        buffer.putShort((channels * Short.SIZE_BYTES).toShort()) /* Block align: NumChannels * BytesPerSample. */
        buffer.putShort(Short.SIZE_BITS.toShort()) /* Bits per sample. */

        /* Data chunk */
        buffer.put("data".toByteArray(Charsets.US_ASCII)) /* Begin of the data chunk. */
        buffer.putInt(subChunk2Length) /* Length of the data chunk. */
    }
}