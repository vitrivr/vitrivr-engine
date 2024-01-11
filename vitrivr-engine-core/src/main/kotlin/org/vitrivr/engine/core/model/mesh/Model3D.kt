package org.vitrivr.engine.core.model.mesh

import org.joml.Vector3f
import java.util.*

/**
 * This class represents a 3d [Model3D]. It comprises a list of [Entity] objects and a list of [Material] objects.
 *The [Entity] objects are used to position and scale the model in the scene. The [Material] objects are used to
 * define the appearance of the model.
 *
 * @version 1.0.0
 * @author Raphael Waltenspuel
 * @author Rahel Arnold
 * @author Ralph Gasser
 */
data class Model3D(
    /** The ID of this [Model3D]. */
    override val id: String,

    /** List of [Entity] objects that define the position and scale of the model. */
    private val entities: MutableList<Entity> = mutableListOf(),

    /**
     * List of [Material] objects that define the appearance of the model.
     * Contains all Meshes and Textures that are used by the model.
     */
    private val materials: MutableList<Material> = mutableListOf()
) : IModel {
    /**
     * Adds an entity to the model and normalizes the model.
     * @param entity Entity to be added.
     */
    fun addEntityNorm(entity: Entity) {
        val mbb = MinimalBoundingBox()

        for (material in this.materials) {
            mbb.merge(material.getMinimalBoundingBox())
        }

        entity.setPosition(mbb.getTranslationToNorm().mul(-1f))
        entity.setScale(mbb.getScalingFactorToNorm())
        entities.add(entity)
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
    override fun getEntities(): List<Entity> = Collections.unmodifiableList(this.entities)

    /**
     * {@inheritDoc}
     */
    override fun getMaterials(): List<Material> = Collections.unmodifiableList(this.materials)

    /**
     * {@inheritDoc}
     */
    override fun getAllNormals(): List<Vector3f> = this.materials.flatMap { mat ->
        mat.meshes.flatMap { mesh -> mesh.vertexNormals }
    }
}
