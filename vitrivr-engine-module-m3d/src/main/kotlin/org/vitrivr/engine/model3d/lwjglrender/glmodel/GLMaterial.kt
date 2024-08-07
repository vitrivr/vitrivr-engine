package org.vitrivr.engine.model3d.lwjglrender.glmodel

import java.util.*
import java.util.function.Consumer
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
class GLMaterial(
    /** The material that is wrapped by this gl material. */
    private val material: Material
) {
  /** The contained meshes in gl context */
  private val meshes: MutableList<GLMesh> = ArrayList()
  /**
   * Returns the gl texture of this gl material.
   *
   * @return The gl texture of this gl material.
   */
  /** The contained texture in gl context */
  val texture: GLTexture

  /**
   * Creates a new GLMaterial from a material.
   *
   * @param material The material that is wrapped by this gl material.
   */
  init {
    material.getMeshes().forEach(Consumer { mesh: Mesh? -> meshes.add(GLMesh(mesh!!)) })
    this.texture = GLTexture(material.materialTexture!!)
  }

  /**
   * Cleans up the gl material and calls all underlying cleanup methods. Removes only the references
   * to wrapped generic meshes and texture. Hence, the material could be used by another extraction
   * task this method does not close the generic meshes or texture.
   */
  fun cleanup() {
    meshes.forEach(Consumer { obj: GLMesh -> obj.cleanup() })
    meshes.clear()
    texture.cleanup()
    LOGGER.trace("Cleaned-up GLMaterial")
  }

  /**
   * Returns the gl meshes of this gl material.
   *
   * @return The unmodifiable list of gl meshes of this gl material.
   */
  fun getMeshes(): List<GLMesh> {
    return Collections.unmodifiableList(this.meshes)
  }

  val diffuseColor: Vector4f
    /**
     * Returns the color from wrapped generic material.
     *
     * @return The color from wrapped generic material. (r,g,b,opacity)
     */
    get() = material.materialDiffuseColor

  companion object {
    private val LOGGER: Logger = LogManager.getLogger()
  }
}
