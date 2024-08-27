package org.vitrivr.engine.model3d.lwjglrender.render

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30
import org.vitrivr.engine.model3d.lwjglrender.window.Window
import org.vitrivr.engine.model3d.lwjglrender.glmodel.GLScene


/**
 * This class holds the render logic for the LWJGL engine
 * Holds the [SceneRender] which loads shaders
 */
class Render {
    /**
     * Instance of the scene render
     * @see SceneRender
     */
    private val sceneRender: SceneRender

    /**
     * Instance of the render options
     * @see RenderOptions
     */
    private var options: RenderOptions? = null

    /**
     * Create a render instance Set up the Render options for OpenGL
     */
    init {
        GL.createCapabilities()
        GL30.glEnable(GL30.GL_DEPTH_TEST)
        GL30.glEnable(GL30.GL_CULL_FACE)
        GL30.glCullFace(GL30.GL_BACK)
        this.sceneRender = SceneRender()
    }

    /**
     * Set the render options [RenderOptions]
     *
     * @param options see [RenderOptions]
     */
    fun setOptions(options: RenderOptions?) {
        this.options = options
    }

    /**
     * Releases all resources
     */
    fun cleanup() {
        sceneRender.cleanup()
        this.options = null
    }

    /**
     * Renders a given Scene in a Given Window
     *
     * @param window GL (offscreen) window instance [Window]
     * @param scene  GL Scene (containing all models) [GLScene]
     */
    fun render(window: Window, scene: GLScene?) {
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT)
        GL30.glViewport(0, 0, window.width, window.height)
        if (scene != null) {
            this.options?.let { sceneRender.render(scene, it) }
        }
    }
}