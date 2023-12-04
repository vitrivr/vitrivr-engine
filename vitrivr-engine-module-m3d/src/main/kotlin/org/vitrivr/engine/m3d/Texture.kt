package org.vitrivr.engine.m3d

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

/**
 * This class represents a texture.
 * In the context free 3D model, a texture is basically a path to a texture file.
 */
class Texture {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /**
         * Default texture path.
         * Points to a png with one white pixel with 100% opacity.
         */
        const val DEFAULT_TEXTURE: String = "./resources/renderer/lwjgl/models/default/default.png"
    }

    /**
     * Path to the texture file.
     */
    private val texturePath: String

    /**
     * Constructor for the Texture class.
     * Sets the texture path to the default texture path.
     */
    constructor() {
        texturePath = DEFAULT_TEXTURE
    }

    /**
     * Constructor for the Texture class.
     * Sets the texture path to the given texture path.
     *
     * @param texturePath Path to the texture file.
     */
    constructor(texturePath: File) {
        this.texturePath = texturePath.toString()
    }

    /**
     * @return Path to the texture file.
     */
    fun getTexturePath(): String {
        return texturePath
    }

    /**
     * Releases all resources associated with this Texture.
     */
    fun close() {
        // Nothing to do here.
        LOGGER.trace("Closing Texture")
    }
}
