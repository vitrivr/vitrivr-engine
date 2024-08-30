package org.vitrivr.engine.model3d.lwjglrender.glmodel

import org.vitrivr.engine.core.model.mesh.texturemodel.Entity

/**
 * The Interface IGLModel provides functionality for an arbitrary model used in the OpenGL context.
 * It is the context-related counterpart to the [IModel] interface.
 */
interface IGLModel {

  /**
   * Returns the entities of the wrapped generic model.
   *
   * @return The entities of the wrapped generic model.
   */
  fun getEntities(): List<Entity>

  /**
   * Adds an entity to the wrapped generic model.
   *
   * @param entity The entity to be added.
   */
  fun addEntity(entity: Entity)

  /**
   * Cleans up the GL model and calls all underlying cleanup methods.
   * Removes only the references to wrapped generic materials
   * Hence, the model could be used by another extraction task this method does not close the generic model.
   */
  fun cleanup()

  /**
   * Returns the ID of the wrapped generic model.
   *
   * @return The ID of the wrapped generic model.
   */
  fun getId(): String

  /**
   * Returns the GL materials of the GL model.
   *
   * @return The GL materials of the GL model.
   */
  fun getMaterials(): List<GLMaterial>
}
