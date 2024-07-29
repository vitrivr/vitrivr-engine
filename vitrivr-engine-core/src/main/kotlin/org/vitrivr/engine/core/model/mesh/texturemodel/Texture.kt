package org.vitrivr.engine.core.model.mesh.texturemodel

import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO


/**
 * This class represents a texture.
 * In the context free 3D model, a texture is basically a path to a texture file.
 */
class Texture : Serializable {
    companion object {

        /**
         * Default texture path.
         * Points to a png with one white pixel with 100% opacity.
         */
        const val DEFAULT_TEXTURE: String = "/renderer/lwjgl/models/default/default.png"
    }

    /**
     * @return Path to the texture file.
     */
    /**
     * Path to the texture file.
     */
    var texturePath: String? = null
        private set

    /**
     * @return BufferedImage texture.
     */
    /**
     * BufferedImages texture.
     */
    var textureImage: BufferedImage? = null
        private set

    /**
     * Constructor for the Texture class.
     * Sets the texture path to the default texture path.
     */
    constructor() {
        this.textureImage = this.javaClass.getResourceAsStream(DEFAULT_TEXTURE).use {
            ImageIO.read(it)
        }
    }

    /**
     * Constructor for the Texture class.
     * Sets the texture path to the given texture path.
     *
     * @param texturePath Path to the texture file.
     */
    constructor(texturePath: String?) {
        this.texturePath = texturePath
    }

    /**
     *
     */
    constructor(image: BufferedImage?) {
        this.textureImage = image
    }


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