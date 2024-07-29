package org.vitrivr.engine.core.model.mesh.texturemodel

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.joml.Vector4f
import org.vitrivr.engine.core.model.mesh.texturemodel.util.MinimalBoundingBox
import java.util.Collections

/**
 * The Material contains all meshes and the texture that are drawn with on the meshes.
 * Further, it contains the diffuse color of the material.
 */
class Material {

    /**
     * List of [Mesh] objects that define the appearance of the model.
     */
    val materialMeshes: MutableList<Mesh> = ArrayList()

    /**
     * Texture that is drawn on all meshes.
     */
    var materialTexture: Texture? = Texture()

    /**
     * diffuseColor is the color that is drawn on the meshes when no texture is present.
     */
    var materialDiffuseColor: Vector4f = DEFAULT_COLOR

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /**
         * DEFAULT_COLOR is black and 100% opaque.
         */
        val DEFAULT_COLOR = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)

        /**
         * Empty material that can be used as a placeholder.
         */
        val EMPTY = Material()
    }

    /**
     * @return A MinimalBoundingBox which encloses all MinimalBoundingBoxes from containing meshes.
     */
    fun getMinimalBoundingBox(): MinimalBoundingBox {
        val mmb = MinimalBoundingBox()
        for (mesh in materialMeshes) {
            mmb.merge(mesh.getMinimalBoundingBox())
        }
        return mmb
    }

    /**
     * @return an unmodifiable list of meshes.
     */
    fun getMeshes(): List<Mesh> = Collections.unmodifiableList(materialMeshes)

    /**
     * @param mesh adds a mesh to the material.
     */
    fun addMesh(mesh: Mesh) {
        materialMeshes.add(mesh)
    }

    /**
     * @param texture sets the texture to this material.
     */
    fun setTexture(texture: Texture) {
        this.materialTexture = texture
    }

    /**
     * @param diffuseColor sets the diffuse color of this material.
     */
    fun setDiffuseColor(diffuseColor: Vector4f) {
        this.materialDiffuseColor = diffuseColor
    }

    /**
     * Closes all resources the material uses.
     * Calls close on all containing classes.
     */
    fun close() {
        materialMeshes.forEach(Mesh::close)
        materialMeshes.clear()
        materialTexture?.close()
        materialTexture = null
        materialDiffuseColor = DEFAULT_COLOR
        LOGGER.trace("Closed Material")
    }
}
