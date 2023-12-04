package org.vitrivr.engine.m3d

import org.apache.logging.log4j.LogManager
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * An Entity in the context of a [Model] describes a position and scale of a model in the scene.
 * The Entity is composed of a model matrix that is used to transform the model in the scene.
 * The model matrix is calculated from the position, rotation and scale of the entity.
 * The Entity influences how the model is rendered in the scene.
 * It does not change the mesh of the model.
 * Neither does it change the viewpoint of the camera.
 */
class Entity(
    /**
     * ID of entity.
     */
    private val id: String,
    /**
     * ID of associated model.
     */
    private val modelId: String
) {
    private val LOGGER = LogManager.getLogger()

    /**
     * Model matrix of entity.
     * Used to transform the model in the scene.
     * Calculated from position, rotation and scale.
     */
    private val modelMatrix: Matrix4f = Matrix4f()

    /**
     * Position of entity.
     */
    private val position: Vector3f = Vector3f()

    /**
     * Rotation of entity.
     */
    private val rotation: Quaternionf = Quaternionf()

    /**
     * Scale of entity.
     */
    private var scale: Float = 1f

    /**
     * Constructs a new Entity.
     * Defines an associated model and an id.
     * With associated model one is able to add new transformations to the Scene [GLScene.addEntity].
     *
     * @param id ID of entity.
     * @param modelId ID of associated model.
     */
    init {
        this.updateModelMatrix()
    }

    /**
     * @return Unique ID of entity.
     */
    fun getId(): String {
        return this.id
    }

    /**
     * @return ID of the associated model.
     */
    fun getModelId(): String {
        return this.modelId
    }

    /**
     * @return Model matrix of entity, describes a rigid transformation of the Model.
     */
    fun getModelMatrix(): Matrix4f {
        return this.modelMatrix
    }

    /**
     * Translation values, contained in the ModelMatrix
     * @return Translate position of entity in x, y, z.
     */
    fun getPosition(): Vector3f {
        return this.position
    }

    /**
     * Rotation values, contained in the ModelMatrix
     * @return Rotation around x,y,z axes as a quaternion.
     */
    fun getRotation(): Quaternionf {
        return this.rotation
    }

    /**
     * Scale value, contained in the ModelMatrix
     * Scales the associated model. 1.0f is no scaling.
     * @return Scale value.
     */
    fun getScale(): Float {
        return this.scale
    }

    /**
     * Sets the as a translation vector from the origin.
     * @param x X coordinate of position.
     * @param y Y coordinate of position.
     * @param z Z coordinate of position.
     */
    @Suppress("unused")
    fun setPosition(x: Float, y: Float, z: Float) {
        this.position.set(x, y, z)
    }

    /**
     * Sets translation vector from the origin.
     * @param position Position of entity.
     */
    fun setPosition(position: Vector3f) {
        this.position.set(position)
    }

    /**
     * Sets the rotation of the entity.
     * @param x X coordinate of axis.
     * @param y Y coordinate of axis.
     * @param z Z coordinate of axis.
     * @param angle Angle of rotation.
     */
    fun setRotation(x: Float, y: Float, z: Float, angle: Float) {
        this.rotation.setAngleAxis(angle, x, y, z)
    }

    /**
     * Sets the rotation of the entity.
     * @param axis Axis of rotation.
     * @param angle Angle of rotation.
     */
    fun setRotation(axis: Vector3f, angle: Float) {
        rotation.fromAxisAngleRad(axis, angle)
    }

    /**
     * Sets the scale of the entity.
     * set to 1 for no scaling.
     * @param scale Scale of entity.
     */
    fun setScale(scale: Float) {
        this.scale = scale
    }

    /**
     * Updates the model matrix of the entity.
     * @implSpec This has to be called after any transformation.
     */
    private fun updateModelMatrix() {
        this.modelMatrix.translationRotateScale(this.position, this.rotation, this.scale)
    }

    /**
     * Closes the entity.
     * Sets the position, rotation to zero and scale to 1.
     */
    fun close() {
        this.position.zero()
        this.rotation.identity()
        this.scale = 1f
        this.updateModelMatrix()
        LOGGER.trace("Entity {} closed", this.id)
    }
}