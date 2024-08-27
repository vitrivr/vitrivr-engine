package org.vitrivr.engine.core.model.mesh.texturemodel

import org.vitrivr.engine.core.model.mesh.texturemodel.util.types.Vec3f
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.vitrivr.engine.core.model.mesh.texturemodel.util.types.Matrix4f
import org.vitrivr.engine.core.model.mesh.texturemodel.util.types.Quaternionf
import kotlinx.serialization.Serializable

/**
 * An Entity in the context of a [Model3d] describes a position and scale of a model in the scene.
 * The Entity is composed of a model matrix that is used to transform the model in the scene.
 * The model matrix is calculated from the position, rotation and scale of the entity.
 * The Entity influences how the model is rendered in the scene.
 * It does not change the mesh of the model.
 * Neither does it change the viewpoint of the camera.
 */
@Serializable
class Entity(val id: String, val modelId: String) {

    /**
     * Model matrix of entity.
     * Used to transform the model in the scene.
     * Calculated from position, rotation and scale.
     */
    val modelMatrix: Matrix4f = Matrix4f()

    /**
     * Position of entity.
     */
    var entityPosition: Vec3f = Vec3f()

    /**
     * Rotation of entity.
     */
    val enttityRotation: Quaternionf = Quaternionf()

    /**
     * Scale of entity.
     */
    var entityScale: Float = 1f

    init {
        updateModelMatrix()
    }

    /**
     * Translation values, contained in the ModelMatrix
     * @return Translativ position of entity in x, y, z.
     */
    fun getPosition(): Vec3f {
        return entityPosition
    }

    /**
     * Rotation values, contained in the ModelMatrix
     * @return Rotation around x,y,z axes as a quaternion.
     */
    fun getRotation(): Quaternionf {
        return enttityRotation
    }

    /**
     * Scale value, contained in the ModelMatrix
     * Scales the associated model. 1.0f is no scaling.
     * @return Scale value.
     */
    fun getScale(): Float {
        return entityScale
    }

    /**
     * Sets the as a translation vector from the origin.
     * @param x X coordinate of position.
     * @param y Y coordinate of position.
     * @param z Z coordinate of position.
     */
    @Suppress("unused")
    fun setPosition(x: Float, y: Float, z: Float) {
        entityPosition.set(x, y, z)
    }

    /**
     * Sets translation vector from the origin.
     * @param position Position of entity.
     */
    fun setPosition(position: Vec3f) {
        this.entityPosition.set(position)
    }

    /**
     * Sets the rotation of the entity.
     * @param x X coordinate of axis.
     * @param y Y coordinate of axis.
     * @param z Z coordinate of axis.
     * @param angle Angle of rotation.
     */
    fun setRotation(x: Float, y: Float, z: Float, angle: Float) {
        enttityRotation.fromAxisAngleRad(x, y, z, angle)
    }

    /**
     * Sets the rotation of the entity.
     * @param axis Axis of rotation.
     * @param angle Angle of rotation.
     */
    fun setRotation(axis: Vec3f, angle: Float) {
        enttityRotation.fromAxisAngleRad(axis, angle)
    }

    /**
     * Sets the scale of the entity.
     * set to 1 for no scaling.
     * @param scale Scale of entity.
     */
    fun setScale(scale: Float) {
        this.entityScale = scale
    }

    /**
     * Updates the model matrix of the entity.
     * This has to be called after any transformation.
     */
    fun updateModelMatrix() {
        modelMatrix.translationRotateScale(entityPosition, enttityRotation, entityScale)
    }

    /**
     * Closes the entity.
     * Sets the position, rotation  to zero and scale to 1.
     */
    fun close() {
        entityPosition.zero()
        enttityRotation.identity()
        entityScale = 1f
        updateModelMatrix()
        LOGGER.trace("Entity {} closed", id)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(Entity::class.java)
    }
}
