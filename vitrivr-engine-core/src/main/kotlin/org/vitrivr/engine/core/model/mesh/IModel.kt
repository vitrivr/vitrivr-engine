package org.vitrivr.engine.core.model.mesh

import org.joml.Vector3f

/**
 *
 */
interface IModel {
    /** The identifier of this [IModel] within the scene. */
    val id: String

    /**
     * Adds an entity to the model.
     * @param entity Entity to be added.
     */
    fun addEntity(entity: Entity)

    /**
     * Returns a list of all entities that are associated with this model.
     *
     * @return List of [Entity] objects.
     */
    fun getEntities(): List<Entity>

    /**
     * Returns a list of all materials that are associated with this model.
     *
     * @return List of [Material] objects.
     */
    fun getMaterials(): List<Material>

    /**
     * Returns a list of all vertex normals that are associated with this model.
     *
     * @return List of [Vector3f].
     */
    fun getAllNormals(): List<Vector3f>
}