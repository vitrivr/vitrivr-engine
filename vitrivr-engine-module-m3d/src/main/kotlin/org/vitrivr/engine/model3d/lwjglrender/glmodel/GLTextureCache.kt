package org.vitrivr.engine.model3d.lwjglrender.glmodel

import org.vitrivr.engine.core.model.mesh.texturemodel.Texture
import java.util.function.Consumer

/**
 * A cache for textures
 * Prevents the same texture from being loaded multiple times
 */
class GLTextureCache {
    /**
     * The cache of textures
     */
    private val textures: MutableMap<String?, GLTexture> =
        HashMap()

    /**
     * Creates a new texture cache
     * Adds a default texture to the cache
     */
    init {
        val texture = Texture()
        textures[texture.texturePath] = GLTexture(texture)
        textures["default"] = GLTexture(texture)
    }

    /**
     * Cleans up the texture cache
     * Cleans the registered textures and clears the cache
     */
    fun cleanup() {
        textures.values.forEach(Consumer { obj: GLTexture -> obj.cleanup() })
        textures.clear()
    }

    /**
     * Adds a texture to the cache if it is not already present
     *
     * @param texture Texture to add
     */
    fun addTextureIfAbsent(texture: GLTexture) {
        textures.putIfAbsent(texture.texturePath, texture)
    }

    /**
     * Returns the gl texture with the given texture path
     *
     * @param texturePath Path of the texture
     * @return The texture with the given texture path
     */
    fun getTexture(texturePath: String?): GLTexture? {
        return textures[texturePath]
    }
}

