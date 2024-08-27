package org.vitrivr.engine.core.model.mesh.texturemodel.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.util.*

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
