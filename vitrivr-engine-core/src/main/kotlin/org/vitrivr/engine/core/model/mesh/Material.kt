package org.vitrivr.engine.core.model.mesh

import org.joml.Vector4f
import java.util.*

/**
 * The Material contains all meshes and the texture that are drawn with on the meshes.
 * Further, it contains the diffuse color of the material.
 */
data class Material(val meshes: MutableList<Mesh> = mutableListOf(), var texture: Texture = Texture(), var diffuseColor: Vector4f = DEFAULT_COLOR) {
    /**
     * Empty material that can be used as a placeholder.
     */
    companion object {
        val DEFAULT_COLOR: Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
    }

    /**
     * @return A [MinimalBoundingBox] which encloses all [MinimalBoundingBoxes][Mesh.getMinimalBoundingBox] from containing meshes.
     */
    fun getMinimalBoundingBox(): MinimalBoundingBox {
        val mmb = MinimalBoundingBox()
        for (mesh in meshes) {
            mmb.merge(mesh.minimalBoundingBox)
        }
        return mmb
    }
}
