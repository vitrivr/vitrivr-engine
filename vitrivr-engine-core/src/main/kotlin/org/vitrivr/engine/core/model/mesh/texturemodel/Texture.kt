package org.vitrivr.engine.core.model.mesh.texturemodel

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.awt.image.BufferedImage
import java.io.*
import java.io.Serializable as JavaSerializable
import javax.imageio.ImageIO

/**
 * This class represents a texture.
 * In the context of a free 3D model, a texture is basically a path to a texture file.
 */
@Serializable
data class Texture(
    var texturePath: String? = null,
    @Contextual
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
