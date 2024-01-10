package org.vitrivr.engine.m3d

import org.joml.Vector3f

interface IModel {

    /**
     * Returns a list of all entities that are associated with this model.
     * @return List of [Entity] objects.
     */
    fun getEntities(): List<Entity>

    /**
     * Adds an entity to the model.
     * @param entity Entity to be added.
     */
    fun addEntity(entity: Entity)

    /**
     * Returns the id of the model.
     * @return ID of the model.
     */
    fun getId(): String

    /**
     * Returns a list of all materials that are associated with this model.
     * @return List of [Material] objects.
     */
    fun getMaterials(): List<Material>

    /**
     * Returns a list of all vertices that are associated with this model.
     * @return List of [Vector3f] objects.
     */
    fun getAllNormals(): List<Vector3f>
}