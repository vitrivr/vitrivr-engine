package org.vitrivr.engine.core.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * A custom [KSerializer] for [BufferedImage] objects.
 *
 * This serializer encodes a [BufferedImage] as a Base64 encoded PNG image.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
object BufferedImageSerializer : KSerializer<BufferedImage> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BufferedImage", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BufferedImage) {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(value, "png", outputStream)
        val base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray())
        encoder.encodeString(base64Image)
    }

    override fun deserialize(decoder: Decoder): BufferedImage {
        val base64Image = decoder.decodeString()
        val imageBytes = Base64.getDecoder().decode(base64Image)
        return ImageIO.read(ByteArrayInputStream(imageBytes))
    }
}
