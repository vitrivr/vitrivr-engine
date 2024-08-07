package org.vitrivr.engine.model3d.lwjglrender.glmodel

import java.util.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector4f
import org.vitrivr.engine.core.model.mesh.texturemodel.Material
import org.vitrivr.engine.core.model.mesh.texturemodel.Mesh

/**
 * The GLMaterial class is a wrapper for the [Material] class.
 * * Material -> GLMaterial( Material )
 *
 * The purpose is to bring the generic Material in an OpenGl context [Material] -> [GLMaterial]
 */
data class GLMaterial(
  /** The material that is wrapped by this gl material. */
  val material: Material,
  /** The contained meshes in gl context */
  val meshes: List<GLMesh> = material.getMeshes().map { GLMesh(it!!) },
  /** The contained texture in gl context */
  val texture: GLTexture = GLTexture(material.materialTexture!!)
) {
  /**
   * Cleans up the gl material and calls all underlying cleanup methods. Removes only the references
   * to wrapped generic meshes and texture. Hence, the material could be used by another extraction
   * task this method does not close the generic meshes or texture.
   */
  fun cleanup() {
    meshes.forEach { it.cleanup() }
    texture.cleanup()
    LOGGER.trace("Cleaned-up GLMaterial")
  }

  /**
   * Returns the color from wrapped generic material.
   *
   * @return The color from wrapped generic material. (r,g,b,opacity)
   */
  val diffuseColor: Vector4f
    get() = material.materialDiffuseColor

  companion object {
    private val LOGGER: Logger = LogManager.getLogger()
  }
}
