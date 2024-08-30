package org.vitrivr.engine.model3d.lwjglrender.glmodel

import org.vitrivr.engine.model3d.lwjglrender.scene.Camera
import org.vitrivr.engine.model3d.lwjglrender.scene.Projection
import org.vitrivr.engine.model3d.lwjglrender.scene.Scene
import org.vitrivr.engine.core.model.mesh.texturemodel.Entity
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel

/**
 * The GLScene class is the topmost class of the GL model hierarchy.
 * The GL model hierarchy is used as a wrapper for the model hierarchy.
 * Therefore, each GL class has a corresponding model class.
 * The generic class has to be provided in the constructor.
 * <ul>
 * <li>Scene -> GLScene( Scene )</li>
 * <li>Model -> GlModel( IModel )</li>
 * <li>Material -> GLMaterial( Material )</li>
 * <li>Mesh -> GLMesh( Mesh )</li>
 * <li>Texture -> GLTexture( Texture )</li>
 * </ul>
 *
 * The purpose is to bring the generic model into an OpenGL context
 * [Scene] -> [GLScene]
 */
class GLScene(private val scene: Scene) {

  /**
   * The wrapped GLModels that are wrapped by this GL scene.
   */
  private val models: MutableMap<String, GLModel> = HashMap()

  /**
   * The texture cache that is used by this GL scene.
   * Textures are cached to avoid loading the same texture multiple times.
   * Has no corresponding generic class.
   */
  private val textureCache: GLTextureCache = GLTextureCache()

  init {
    updateGlSceneFromScene()
  }

  /**
   * Adds a model to the scene.
   *
   * @param model The model that is added to the scene.
   */
  fun addModel(model: IModel) {
    scene.addModel(model)
    updateGlSceneFromScene()
  }

  /**
   * Updates the GL scene from the scene.
   * It updates the GL scene content to match the scene content.
   */
  private fun updateGlSceneFromScene() {
    scene.getModels().forEach { (k, v) ->
      models.putIfAbsent(k, GLModel(v))
    }
    models.forEach { (_, v) ->
      v.getMaterials()?.forEach { mat ->
          if (mat != null) {
            textureCache.addTextureIfAbsent(mat.texture)
          }
      }
    }
  }

  /**
   * Adds an entity to the corresponding model.
   * @param entity The entity that is added to the model.
   */
  fun addEntity(entity: Entity) {
    val modelId = entity.modelId
    val model = models[modelId] ?: throw RuntimeException("Model not found: $modelId")
    model.addEntity(entity)
  }

  /**
   * Returns the GL models of the GL scene.
   * @return The GL models of the GL scene.
   */
  fun getModels(): Map<String, IGLModel> {
    return models
  }

  /**
   * Returns the texture cache of the GL scene.
   * @return The texture cache of the GL scene.
   */
  fun getTextureCache(): GLTextureCache {
    return textureCache
  }

  /**
   * Returns the projection of the wrapped generic scene.
   * @return The projection of the wrapped generic scene.
   */
  fun getProjection(): Projection {
    return scene.getProjection()
  }

  /**
   * Returns the camera of the wrapped generic scene.
   * @return The camera of the wrapped generic scene.
   */
  fun getCamera(): Camera {
    return scene.getCamera()
  }

  /**
   * Clears the models of the GL scene but not containing resources.
   * Removes the references to the wrapped generic models and textures.
   * Hence, the models could be used by another extraction task this method does not close the models or textures.
   * Can be used to only remove models temporarily from GL scene.
   */
  fun clearModels() {
    cleanup()
    models.clear()
  }

  /**
   * Cleans up the GL scene and calls all underlying cleanup methods.
   * Removes only the references to wrapped generic models and textures.
   * Hence, the model could be used by another extraction task this method does not close the generic models or textures.
   */
  fun cleanup() {
    models.values.forEach { it.cleanup() }
    models.clear()
    textureCache.cleanup()
  }

  /**
   * Resizes the projection of the wrapped generic scene.
   * @param width The new width of the projection.
   * @param height The new height of the projection.
   */
  fun resize(width: Int, height: Int) {
    scene.getProjection().updateProjMatrix(width, height)
  }
}
