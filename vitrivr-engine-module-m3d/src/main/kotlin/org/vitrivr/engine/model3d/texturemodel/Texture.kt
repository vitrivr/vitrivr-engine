package org.vitrivr.engine.model3d.texturemodel

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.image.BufferedImage

/**
 * This class represents a texture.
 * In the context free 3D model, a texture is basically a path to a texture file.
 */
class Texture {
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
        this.texturePath = DEFAULT_TEXTURE
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

    constructor(image: BufferedImage?) {
        this.textureImage = image
    }

    /**
     * Releases all resources associated with this Texture.
     */
    fun close() {
        // Nothing to do here.
        LOGGER.trace("Closing Texture")
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /**
         * Default texture path.
         * Points to a png with one white pixel with 100% opacity.
         */
        const val DEFAULT_TEXTURE: String =
            "vitrivr-engine-module-m3d/src/main/resources/renderer/lwjgl/models/default/default.png"
    }
}