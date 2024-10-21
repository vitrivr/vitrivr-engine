package org.vitrivr.engine.model3d.lwjglrender.glmodel

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.vitrivr.engine.core.model.mesh.texturemodel.Entity
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel

/**
 * The GLModel class is a wrapper for the [IModel] class.
 * <ul>
 * <li>IModel -> GLModel( IModel )</li>
 * </ul>
 *
 * The purpose is to bring the generic IModel into an OpenGL context
 * [IModel] -> [GLModel]
 */
class GLModel(private val model: IModel) : IGLModel {

  /**
   * The contained materials in GL context
   */
  private val materials: MutableList<GLMaterial> = mutableListOf()

  init {
    model.getMaterials().forEach { material ->
      materials.add(GLMaterial(material))
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun getEntities(): List<Entity> {
    return model.getEntities()
  }

  /**
   * {@inheritDoc}
   */
  override fun addEntity(entity: Entity) {
    model.addEntity(entity)
  }

  /**
   * {@inheritDoc}
   */
  override fun cleanup() {
    materials.forEach { it.cleanup() }
    materials.clear()
    LOGGER.debug("GLModel cleaned up")
  }

  /**
   * {@inheritDoc}
   */
  override fun getId(): String {
    return model.getId()
  }

  /**
   * {@inheritDoc}
   */
  override fun getMaterials(): List<GLMaterial> {
    return materials
  }

  companion object {
    private val LOGGER: Logger = LogManager.getLogger(GLModel::class.java)
  }
}
