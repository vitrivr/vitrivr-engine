package org.vitrivr.engine.core.model.content.element.m3d

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.joml.Vector4f
import org.vitrivr.engine.core.model.content.element.m3d.util.MinimalBoundingBox
import java.util.*

/**
 * The Material contains all meshes and the texture that are drawn with on the meshes.
 * Further, it contains the diffuse color of the material.
 */
class Material {

    private val LOGGER: Logger = LogManager.getLogger()

    /**
     * List of [Mesh] objects that define the appearance of the model.
     */
    val myMeshes: MutableList<Mesh> = ArrayList()

    /**
     * Texture that is drawn on all meshes.
     */
    var myTexture: Texture = Texture()

    /**
     * DEFAULT_COLOR is black and 100% opaque.
     */
    val DEFAULT_COLOR: Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)

    /**
     * Diffuse color is the color that is drawn on the meshes when no texture is present.
     */
    var myDiffuseColor: Vector4f = DEFAULT_COLOR

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
        for (mesh in myMeshes) {
            mmb.merge(mesh.getMinimalBoundingBox())
        }
        return mmb
    }

    /**
     * @return the scaling factor to norm 1 size from all containing meshes merged.
     * @deprecated use [getMinimalBoundingBox] instead.
     */
    @Deprecated("Use getMinimalBoundingBox() instead.")
    fun getMaxNormalizedScalingFactor(): Float {
        var min = Float.MAX_VALUE
        for (mesh in myMeshes) {
            min = min.coerceAtMost(mesh.getNormalizedScalingFactor())
        }
        return min
    }

    /**
     * @return the translation to origin (0,0,0) from all containing meshes merged.
     * @deprecated use [getMinimalBoundingBox] instead.
     */
    @Deprecated("Use getMinimalBoundingBox() instead.")
    fun getMaxNormalizedPosition(): Vector3f {
        var min = Vector3f()
        for (mesh in myMeshes) {
            min = if (min.length() > mesh.getNormalizedPosition().length()) min else mesh.getNormalizedPosition()
        }
        return min
    }

    /**
     * @return an unmodifiable list of meshes.
     */
    fun getMeshes(): List<Mesh> {
        return Collections.unmodifiableList(myMeshes)
    }

    /**
     * @param mesh adds a mesh to the material.
     */
    fun addMesh(mesh: Mesh) {
        myMeshes.add(mesh)
    }

    /**
     * @return the texture of this material.
     */
    fun getTexture(): Texture {
        return myTexture
    }

    /**
     * @param texture sets the texture of this material.
     */
    fun setTexture(texture: Texture) {
        this.myTexture = texture
    }

    /**
     * @return the diffuse color of this material.
     */
    fun getDiffuseColor(): Vector4f {
        return myDiffuseColor
    }

    /**
     * @param diffuseColor sets the diffuse color of this material.
     */
    fun setDiffuseColor(diffuseColor: Vector4f) {
        this.myDiffuseColor = diffuseColor
    }

    /**
     * Closes all resources the material uses.
     * Calls close on all containing classes.
     */
    fun close() {
        myMeshes.forEach(Mesh::close)
        myMeshes.clear()
        myTexture.close()
        myTexture = Texture()
        myDiffuseColor = DEFAULT_COLOR
        LOGGER.trace("Closed Material")
    }
}
