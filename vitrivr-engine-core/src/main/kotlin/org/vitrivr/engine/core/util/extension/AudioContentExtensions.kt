package org.vitrivr.engine.core.util.extension

import org.vitrivr.engine.core.model.content.element.AudioContent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private fun writeWaveHeader(buffer: ByteBuffer, samplingRate: Float, channels: Short, length: Int) {
    val subChunk2Length = length * channels * (16 / 8) // Assuming 16 bits per sample

    // RIFF Chunk
    buffer.put("RIFF".toByteArray())
    buffer.putInt(36 + subChunk2Length)
    buffer.put("WAVE".toByteArray())

    // fmt chunk
    buffer.put("fmt ".toByteArray())
    buffer.putInt(16) // PCM header size
    buffer.putShort(1) // Audio format 1 = PCM
    buffer.putShort(channels)
    buffer.putInt(samplingRate.toInt())
    buffer.putInt((samplingRate * channels * (16 / 8)).toInt()) // Byte rate
    buffer.putShort((channels * (16 / 8)).toShort()) // Block align
    buffer.putShort(16) // Bits per sample

    // data chunk
    buffer.put("data".toByteArray())
    buffer.putInt(subChunk2Length)
}


fun AudioContent.toDataURL(): String {
    val data = this.content
    val buffer = ByteBuffer.allocate(44 + data.remaining() * 2).order(ByteOrder.LITTLE_ENDIAN)

    // Write WAV header
    writeWaveHeader(buffer, this.samplingRate.toFloat(), 1, data.remaining())

    while (data.hasRemaining()) {
        val sample = data.get()
        buffer.putShort(sample)
    }

    val base64 = Base64.getEncoder().encodeToString(buffer.array())
    return "data:audio/wav;base64,$base64"
}
