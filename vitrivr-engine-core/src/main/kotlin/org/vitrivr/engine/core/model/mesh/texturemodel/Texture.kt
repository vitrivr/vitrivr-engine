package org.vitrivr.engine.core.model.mesh.texturemodel

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.serializer.BufferedImageSerializer
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO
import java.io.Serializable as JavaSerializable

/**
 * This class represents a texture.
 * In the context of a free 3D model, a texture is basically a path to a texture file.
 */
@Serializable
data class Texture(
    var texturePath: String? = null,

    @Serializable(with = BufferedImageSerializer::class)
    var textureImage: BufferedImage? = null
) : JavaSerializable {

    companion object {
        /**
         * Default texture path.
         * Points to a png with one white pixel with 100% opacity.
         */
        const val DEFAULT_TEXTURE: String = "/renderer/lwjgl/models/default/default.png"
    }

    init {
        if (texturePath == null && textureImage == null) {
            this.textureImage = this.javaClass.getResourceAsStream(DEFAULT_TEXTURE).use {
                ImageIO.read(it)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Texture) return false

        if (texturePath != other.texturePath) return false
        if (!compareBufferedImages(textureImage, other.textureImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = texturePath?.hashCode() ?: 0
        result = 31 * result + (textureImage?.let { it.hashCode() } ?: 0)
        return result
    }

    /**
     * Compares two BufferedImages pixel by pixel.
     */
    private fun compareBufferedImages(img1: BufferedImage?, img2: BufferedImage?): Boolean {
        if (img1 == null || img2 == null) return img1 == img2
        if (img1.width != img2.width || img1.height != img2.height) return false

        for (y in 0 until img1.height) {
            for (x in 0 until img1.width) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false
                }
            }
        }

        return true
    }

    constructor(texturePath: String) : this(texturePath, null)

    constructor(textureImage: BufferedImage) : this(null, textureImage)

    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        if (this.texturePath != null) {
            oos.writeShort(0)
            val bytes = this.texturePath!!.toByteArray()
            oos.writeInt(bytes.size)
            oos.write(bytes)
        } else if (this.textureImage != null) {
            val baos = ByteArrayOutputStream()
            ImageIO.write(this.textureImage, "png", baos)
            val bytes = baos.toByteArray()
            oos.writeShort(1)
            oos.writeInt(bytes.size)
            oos.write(bytes)
        }
    }

    @Throws(IOException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val mode: Short = `in`.readShort()
        val length: Int = `in`.readInt()
        val bytes = ByteArray(length)
        `in`.readFully(bytes)
        if (mode == 0.toShort()) {
            this.texturePath = String(bytes)
        } else if (mode == 1.toShort()) {
            this.textureImage = ImageIO.read(ByteArrayInputStream(bytes))
        }
    }
}
