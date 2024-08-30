package org.vitrivr.engine.model3d.lwjglrender.scene

import org.vitrivr.engine.core.model.mesh.texturemodel.Entity
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel
import org.vitrivr.engine.model3d.lwjglrender.scene.Camera
import org.vitrivr.engine.model3d.lwjglrender.scene.Projection

/**
 * The Scene class holds generic 3D scene elements (models, etc.).
 * A scene consists of a model, a camera, and a projection.
 */
class Scene(width: Int, height: Int) {

    /**
     * Map of generic models in the scene.
     */
    private val models: MutableMap<String, IModel> = mutableMapOf()

    /**
     * Projection of the scene.
     */
    private val projection: Projection = Projection(width, height)

    /**
     * Camera of the scene.
     */
    private val camera: Camera = Camera()

    /**
     * Add an entity to the corresponding model.
     * Can be used to resize the scene before GL context is created.
     */
    fun addEntity(entity: Entity) {
        val modelId = entity.modelId
        val model = models[modelId]
        requireNotNull(model) { "Model not found: $modelId" }
        model.addEntity(entity)
    }

    /**
     * Add a model to the scene.
     */
    fun addModel(model: IModel) {
        models[model.getId()] = model
    }

    /**
     * Get a model from the scene.
     */
    fun getModels(): Map<String, IModel> = models

    /**
     * Get the projection of the scene.
     */
    fun getProjection(): Projection = projection

    /**
     * Get the camera of the scene.
     */
    fun getCamera(): Camera = camera

    /**
     * Resizes the scene.
     * Can be used to resize the scene before GL context is created.
     */
    fun resize(width: Int, height: Int) {
        projection.updateProjMatrix(width, height)
    }
}
