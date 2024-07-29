package org.vitrivr.engine.core.model.mesh.texturemodel

import java.util.*
import java.util.function.Consumer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.util.MinimalBoundingBox

/**
 * This class represents a 3d model that can be rendered by the [Engine]. The 3d3 model is composed of a
 * list of [Entity] objects and a list of [Material] objects. The [Entity] objects are used to
 * position and scale the 3d model in the scene. The [Material] objects are used to define the
 * appearance of the 3d model.
 */
data class Model3d(
  /** ID of the 3d model. */
    val modelId: String,
  /**
     * List of [Material] objects that define the appearance of the model. Contains all Meshes and
     * Textures that are used by the 3d model.
     */
    val modelMaterials: MutableList<Material>
) : IModel {
  /** List of [Entity] objects that define the position and scale of the 3d model. */
  private val entities: MutableList<Entity> = ArrayList()

  /** {@inheritDoc} */
  override fun getEntities(): List<Entity> {
    return Collections.unmodifiableList(this.entities)
  }

  /** {@inheritDoc} */
  override fun addEntity(entity: Entity) {
    entities.add(entity)
  }

  /**
   * Adds an entity to the model and normalizes the 3d model.
   *
   * @param entity Entity to be added.
   */
  fun addEntityNorm(entity: Entity) {
    val mbb = MinimalBoundingBox()

    for (material in this.modelMaterials) {
      mbb.merge(material.getMinimalBoundingBox())
    }

    entity.entityPosition = mbb.translationToNorm.mul(-1f)
    entity.entityScale = mbb.scalingFactorToNorm
    entities.add(entity)
  }

  /** {@inheritDoc} */
  override fun getId(): String {
    return this.modelId
  }

  /** {@inheritDoc} */
  override fun getMaterials(): List<Material> {
    return Collections.unmodifiableList(this.modelMaterials)
  }

  /** {@inheritDoc} */
  override fun getAllNormals(): List<Vector3f> {
    val normals = ArrayList<Vector3f>()
    modelMaterials.forEach(
        Consumer { m: Material ->
          m.materialMeshes.forEach(Consumer { mesh: Mesh -> normals.addAll(mesh.getNormals()) })
        })
    return normals
  }

  /** Closes the 3d model and releases all resources. */
  fun close() {
    modelMaterials.forEach(Consumer { obj: Material -> obj.close() })
    modelMaterials.clear()
    entities.forEach(Consumer { obj: Entity -> obj.close() })
    entities.clear()
    LOGGER.trace("Closed model {}", this.modelId)
  }

  companion object {
    private val LOGGER: Logger = LogManager.getLogger()

    /**
     * Empty 3d model that can be used as a placeholder.
     */
    val EMPTY = Model3d("EmptyModel", listOf(Material.EMPTY).toMutableList())
  }
}
