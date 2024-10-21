package org.vitrivr.engine.model3d.lwjglrender.engine

import org.vitrivr.engine.model3d.lwjglrender.glmodel.GLScene
import org.vitrivr.engine.model3d.lwjglrender.render.Render
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import org.vitrivr.engine.model3d.lwjglrender.scene.Camera
import org.vitrivr.engine.model3d.lwjglrender.scene.Scene
import org.vitrivr.engine.model3d.lwjglrender.window.Window
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions

/**
 * The engine is the main class of the rendering engine. It holds the window, the scene, and the
 * render object. It provides a render loop for continuous rendering and a runOnce method to render
 * a single frame rendering.
 */
class Engine(windowTitle: String, opts: WindowOptions, private val appLogic: EngineLogic) {
  /** The window object. */
  private val window: Window =
      Window(windowTitle, opts) {
        resize()
        null
      }

  /** Indicates whether the engine is running in continuous rendering mode. */
  private var running: Boolean = true

  /** The render object. */
  private val render: Render = Render()

  /** The scene object. */
  private val scene: GLScene = GLScene(Scene(window.width, window.height))

  /** The target frames per second. */
  private val targetFps: Int = opts.fps

  /** The target updates per second. (e.g. inputs rotation, etc.) */
  private val targetUps: Int = opts.ups

  init {
    appLogic.init(window, scene, render)
  }

  /**
   * Sets the render options. Must be called before render is called.
   *
   * @param options The render options.
   */
  fun setRenderOptions(options: RenderOptions) {
    render.setOptions(options)
  }

  /**
   * Refreshes the engine. Is called when the engine is stopped and has to be ready to start again.
   */
  fun refresh() {
    appLogic.cleanup()
    render.cleanup()
    scene.cleanup()
  }

  /**
   * Releases all resources and terminates the engine. Is called when the engine is stopped and all
   * resources have to be released.
   */
  fun clear() {
    appLogic.cleanup()
    render.cleanup()
    scene.cleanup()
    window.cleanup()
  }

  /** Starts the engine in continuous rendering mode. */
  fun start() {
    running = true
    run()
  }

  /** Stops the continuous rendering mode. */
  fun stop() {
    running = false
  }

  /** Runs a single frame rendering. */
  fun runOnce() {
    window.pollEvents()
    appLogic.beforeRender(window, scene, render)
    render.render(window, scene)
    appLogic.afterRender(window, scene, render)
    window.update()
  }

  /**
   * Run mode runs permanently until the engine is stopped.
   * 1. Poll events
   * 2. Input
   * 3. Update
   * 4. Render
   * 5. Update window
   */
  fun run() {
    var initialTime = System.currentTimeMillis()
    // maximum elapsed time between updates
    val timeU = 1000.0f / targetUps
    // maximum elapsed time between render calls
    val timeR = if (targetFps > 0) 1000.0f / targetFps else 0f
    var deltaUpdate = 0.0f
    var deltaFps = 0.0f

    var updateTime = initialTime

    while (running && !window.windowShouldClose()) {
      window.pollEvents()

      val now = System.currentTimeMillis()

      // relation between actual and elapsed time. 1 if equal.
      deltaUpdate += (now - initialTime) / timeU
      deltaFps += (now - initialTime) / timeR

      // If passed maximum elapsed time for render, process user input
      if (targetFps <= 0 || deltaFps >= 1) {
        appLogic.input(window, scene, now - initialTime)
      }

      // If passed maximum elapsed time for update, update the scene
      if (deltaUpdate >= 1) {
        val diffTimeMillis = now - updateTime
        appLogic.update(window, scene, diffTimeMillis)
        updateTime = now
        deltaUpdate--
      }

      // If passed maximum elapsed time for render, render the scene
      if (targetFps <= 0 || deltaFps >= 1) {
        appLogic.beforeRender(window, scene, render)
        render.render(window, scene)
        deltaFps--
        window.update()
        appLogic.afterRender(window, scene, render)
      }
    }
    refresh()
  }

  /** Resizes the window. */
  fun resize() {
    scene.resize(window.width, window.height)
  }

  /**
   * Returns the camera object.
   *
   * @return The camera object.
   */
  fun getCamera(): Camera {
    return scene.getCamera()
  }

  /**
   * Returns the window object.
   *
   * @return The window object.
   */
  fun getWindow(): Window {
    return window
  }

  /**
   * Returns the scene object.
   *
   * @return The scene object.
   */
  fun getScene(): GLScene {
    return scene
  }
}
