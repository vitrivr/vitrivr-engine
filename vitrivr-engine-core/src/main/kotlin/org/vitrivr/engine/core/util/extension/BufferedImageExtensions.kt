package org.vitrivr.engine.core.util.extension

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO

fun BufferedImage.getRGBArray(): IntArray = this.getRGB(0, 0, this.width, this.height, null, 0, this.width)

fun BufferedImage.setRGBArray(array: IntArray) = this.setRGB(0, 0, this.width, this.height, array, 0, this.width)


/**
 *
 */
fun BufferedImage(dataUrl: String) : BufferedImage {
    val base64 = dataUrl.substringAfter("base64,")
    val bytes = Base64.getDecoder().decode(base64)
    return ImageIO.read(ByteArrayInputStream(bytes))
}