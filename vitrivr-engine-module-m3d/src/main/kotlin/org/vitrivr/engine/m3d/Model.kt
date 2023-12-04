package org.vitrivr.engine.m3d

import org.apache.logging.log4j.LogManager
import org.joml.Vector3f
import org.vitrivr.engine.m3d.util.MinimalBoundingBox

/**
 * This class represents a 3d model.
 * The model is composed of a list of [Entity] objects
 * and a list of [Material] objects.
 * The [Entity] objects are used to position and scale the model in the scene.
 * The [Material] objects are used to define the appearance of the model.
 */
class Model(id: String, materials: MutableList<Material>) : IModel {
    private val LOGGER = LogManager.getLogger()

    /**
     * ID of the model.
     */
    private val id: String

    /**
     * List of [Entity] objects that define the position and scale of the model.
     */
    private val entities: MutableList<Entity>

    /**
     * List of [Material] objects that define the appearance of the model.
     * Contains all Meshes and Textures that are used by the model.
     */
    val mat: MutableList<Material>

    /**
     * Empty model that can be used as a placeholder.
     */
    init {
        this.id = id
        this.entities = ArrayList()
        this.mat = materials
    }

    /**
     * Adds an entity to the model and normalizes the model.
     * @param entity Entity to be added.
     */
    fun addEntityNorm(entity: Entity) {
        val mbb = MinimalBoundingBox()

        for (material in mat) {
            mbb.merge(material.getMinimalBoundingBox())
        }

        entity.setPosition(mbb.getTranslationToNorm().mul(-1f))
        entity.setScale(mbb.getScalingFactorToNorm())
        entities.add(entity)
    }

    /**
     * {@inheritDoc}
     */
    override fun getEntities(): List<Entity> {
        return entities.toList()
    }

    /**
     * {@inheritDoc}
     */
    override fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    /**
     * {@inheritDoc}
     */
    override fun getId(): String {
        return id
    }

    /**
     * {@inheritDoc}
     */
    override fun getMaterials(): List<Material> {
        return mat.toList()
    }

    /**
     * {@inheritDoc}
     */
    override fun getAllNormals(): List<Vector3f> {
        val normals = ArrayList<Vector3f>()
        mat.forEach { m -> m.myMeshes.forEach { mesh -> normals.addAll(mesh.getNormals()) } }
        return normals
    }

    /**
     * Closes the model and releases all resources.
     */
    fun close() {
        mat.forEach { it.close() }
        mat.clear()
        entities.forEach { it.close() }
        entities.clear()
        LOGGER.trace("Closed model {}", id)
    }
}
