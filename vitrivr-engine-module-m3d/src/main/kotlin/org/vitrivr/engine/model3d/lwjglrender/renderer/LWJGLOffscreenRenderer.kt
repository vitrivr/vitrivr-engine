package org.vitrivr.engine.model3d.lwjglrender.renderer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.Entity
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.model3d.lwjglrender.engine.Engine
import org.vitrivr.engine.model3d.lwjglrender.engine.EngineLogic
import org.vitrivr.engine.model3d.lwjglrender.glmodel.GLScene
import org.vitrivr.engine.model3d.lwjglrender.glmodel.IGLModel
import org.vitrivr.engine.model3d.lwjglrender.window.Window
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import org.vitrivr.engine.model3d.lwjglrender.render.Render
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import org.vitrivr.engine.model3d.lwjglrender.scene.*
import java.awt.image.BufferedImage
import java.util.concurrent.LinkedTransferQueue
import java.util.function.Consumer

/**
 * This is the top most class of the LWJGL for Java 3D renderer. Its main function is to provide an interface between Engine and the outside world. It extends the abstract class [EngineLogic] which allows the instanced engine to call methods depending on the engine state.
 *
 * @version 1.0.0
 * @author Raphael Waltenspühl
 */
class LWJGLOffscreenRenderer : EngineLogic() {
    /**
     * The (offscreen) window options for the engine.
     */
    private var windowOptions: WindowOptions? = null

    /**
     * The engine instance.
     */
    private var engine: Engine? = null

    /**
     * The model queue. From this queue the renderer takes the next model to render.
     */
    private val modelQueue = LinkedTransferQueue<IModel>()

    /**
     * The image queue. In this queue the renderer puts the rendered images.
     */
    private val imageQueue = LinkedTransferQueue<BufferedImage>()


    /**
     * Constructor for the LWJGLOffscreenRenderer. Initializes the model queue and the image queue.
     */
    init {
        LOGGER.trace("LWJGLOffscreenRenderer created")
    }

    /**
     * Sets the window options for the engine.
     *
     * @param opts The window options.
     */
    fun setWindowOptions(opts: WindowOptions?) {
        this.windowOptions = opts
    }

    /**
     * Sets the render options for the engine.
     *
     * @param opts The render options.
     */
    fun setRenderOptions(opts: RenderOptions?) {
        if (opts != null) {
            engine!!.setRenderOptions(opts)
        }
    }

    /**
     * Starts the engine with given window options. Registers the LWJGLOffscreenRenderer as the engine logic.
     */
    fun startEngine() {
        val name = "LWJGLOffscreenRenderer"
        this.engine = this.windowOptions?.let { Engine(name, it, this) }
    }

    /**
     * Starts the rendering process.
     */
    fun render() {
        engine!!.runOnce()
        LOGGER.trace("LWJGLOffscreenRenderer rendered")
    }

    /**
     * Is called once at the initialization of the engine. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
     */
    override fun init(window: Window, scene: GLScene, render: Render) {
        scene.getCamera().setPosition(0f, 0f, 1f)
    }

    /**
     * Is called from the engine before the render method. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
     */
    override fun beforeRender(window: Window, scene: GLScene, render: Render) {
        this.loadNextModelFromQueueToScene(window, scene)
    }

    /**
     * Is called from the engine after the render method. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
     */
    override fun afterRender(window: Window, scene: GLScene, render: Render) {
        val lfc = LightfieldCamera(this.windowOptions!!)
        imageQueue.add(lfc.takeLightfieldImage())
    }

    /**
     * Is called from the engine as first step during refresh and cleanup DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
     */
    override fun cleanup() {
        LOGGER.trace("LWJGLOffscreenRenderer cleaned")
    }


    /**
     * This method is called every frame. This is only used in continuous rendering. The purpose is to do some input handling. Could be use for optimize view  angles on a fast manner. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
     */
    override fun input(window: Window, scene: GLScene, diffTimeMillis: Long) {
        scene.getModels().forEach { (k: String?, v: IGLModel) ->
            v.getEntities().forEach(
                Consumer { obj: Entity -> obj.updateModelMatrix() })
        }
    }

    /**
     * After Engine run This method is called every frame. This is only used in continuous rendering. The purpose is to process some life output. Could be use for optimize view  angles on a fast manner. DO NOT CALL ENGINE METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM THIS CLASS
     */
    override fun update(window: Window, scene: GLScene, diffTimeMillis: Long) {
    }

    /**
     * This method is called to load the next model into the provided scene.
     *
     * @param scene The scene to put the model in.
     */
    @Suppress("unused")
    private fun loadNextModelFromQueueToScene(window: Window, scene: GLScene) {
        if (!modelQueue.isEmpty()) {
            val model = modelQueue.poll() as Model3d
            if (model.getEntities().isEmpty()) {
                val entity = Entity("cube", model.getId())
                model.addEntityNorm(entity)
            }
            //cleans all current models from the scene
            scene.cleanup()
            //adds the new model to the scene
            scene.addModel(model)
        }
        scene.getModels().forEach { (k: String?, v: IGLModel) ->
            v.getEntities().forEach(
                Consumer { obj: Entity -> obj.updateModelMatrix() })
        }
    }

    /**
     * Moves the camera in the scene with given deltas in cartesian coordinates. Look at the origin.
     */
    fun moveCameraOrbit(dx: Float, dy: Float, dz: Float) {
        engine!!.getCamera().moveOrbit(dx, dy, dz)
    }

    /**
     * Sets the camera in the scene to cartesian coordinates. Look at the origin.
     */
    fun setCameraOrbit(x: Float, y: Float, z: Float) {
        engine!!.getCamera().setOrbit(x, y, z)
    }

    /**
     * Moves the camera in the scene with given deltas in cartesian coordinates. Keep the orientation.
     */
    @Suppress("unused")
    fun setCameraPosition(x: Float, y: Float, z: Float) {
        engine!!.getCamera().setPosition(x, y, z)
    }

    /**
     * Set position of the camera and look at the origin. Camera will stay aligned to the y plane.
     */
    fun lookFromAtO(x: Float, y: Float, z: Float) {
        val lookFrom = Vector3f(x, y, z)
        val lookAt = Vector3f(0f, 0f, 0f)

        engine!!.getCamera().setPositionAndOrientation(lookFrom, lookAt)
    }

    @get:Suppress("unused")
    val aspect: Float
        /**
         * Returns the aspect ratio of the window.
         */
        get() = windowOptions!!.width.toFloat() / windowOptions!!.height.toFloat()

    /**
     * Interface to outside to add a model to the scene.
     */
    fun assemble(model: IModel) {
        modelQueue.add(model)
    }

    /**
     * Interface to outside to get a rendered image.
     */
    fun obtain(): BufferedImage {
        return imageQueue.poll()
    }

    /**
     * This method disposes the engine. Window is destroyed and all resources are freed.
     */
    fun clear() {
        engine!!.clear()
        this.engine = null
    }

    val width: Int
        /**
         * Returns the width of the window.
         *
         * @return The width of the window. (in pixels)
         */
        get() = windowOptions!!.width

    val height: Int
        /**
         * Returns the height of the window.
         *
         * @return The height of the window. (in pixels)
         */
        get() = windowOptions!!.height

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
}