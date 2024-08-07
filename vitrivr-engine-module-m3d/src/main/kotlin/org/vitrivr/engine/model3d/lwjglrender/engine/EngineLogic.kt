package org.vitrivr.engine.model3d.lwjglrender.engine

import org.vitrivr.engine.model3d.lwjglrender.glmodel.GLScene
import org.vitrivr.engine.model3d.lwjglrender.render.Render
import org.vitrivr.engine.model3d.lwjglrender.window.Window

/** The EngineLogic provides methods to be called by the engine on certain states. */
abstract class EngineLogic {

  /**
   * Is called from the engine as first step during refresh and cleanup. @implSpec DO NOT CALL
   * ENGINE LOGIC METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  internal abstract fun cleanup()

  /**
   * Is called once at the initialization of the engine. @implSpec DO NOT CALL ENGINE LOGIC METHODS
   * IN THIS METHOD DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  internal abstract fun init(window: Window, scene: GLScene, render: Render)

  /**
   * Is called from the engine before the render method. @implSpec DO NOT CALL ENGINE LOGIC METHODS
   * IN THIS METHOD DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  internal abstract fun beforeRender(window: Window, scene: GLScene, render: Render)

  /**
   * Is called from the engine after the render method. @implSpec DO NOT CALL ENGINE LOGIC METHODS
   * IN THIS METHOD DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  internal abstract fun afterRender(window: Window, scene: GLScene, render: Render)

  /**
   * This method is called every frame. This is only used in continuous rendering. The purpose is to
   * do some input handling. Could be used to optimize view angles in a fast manner. @implSpec DO
   * NOT CALL ENGINE LOGIC METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM EXTENDING CLASS
   */
  internal abstract fun input(window: Window, scene: GLScene, diffTimeMillis: Long)

  /**
   * After Engine run this method is called every frame. This is only used in continuous rendering.
   * The purpose is to process some live output. Could be used to optimize view angles in a fast
   * manner. @implSpec DO NOT CALL ENGINE LOGIC METHODS IN THIS METHOD DO NOT CALL THIS METHOD FROM
   * EXTENDING CLASS
   */
  internal abstract fun update(window: Window, scene: GLScene, diffTimeMillis: Long)
}
