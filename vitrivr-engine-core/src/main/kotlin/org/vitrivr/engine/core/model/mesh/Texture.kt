package org.vitrivr.engine.core.model.mesh


import java.io.File
import java.nio.file.Path

/**
 * This class represents a [Texture]. In the context free 3D model, a texture is basically a path to a texture file.
 *
 * @version 1.0.0
 * @author Raphael Waltenspuel
 * @author Rahel Arnold
 * @author Ralph Gasser
 */
data class Texture(val path: Path = DEFAULT_TEXTURE) {
    companion object {
        /** Default texture path. Points to a png with one white pixel with 100% opacity. */
        val DEFAULT_TEXTURE: Path = Path.of("vitrivr-engine-module-m3d/src/main/resources/renderer/lwjgl/models/default/default.png")
    }

    /**
     * Constructor for the Texture class.
     * Sets the texture path to the given texture path.
     *
     * @param texturePath Path to the texture file.
     */
    constructor(texturePath: File) : this(texturePath.toPath())
}
