package org.vitrivr.engine.model3d.lwjglrender.renderer

/**
 * States of the Render Workflow
 * [RenderWorker]
 */
object RenderStates {
    const val IDLE: String = "IDLE"
    const val INIT_WINDOW: String = "INIT_WINDOW"
    const val INIT_RENDERER: String = "INIT_RENDERER"
    const val LOAD_MODEL: String = "INIT_MODEL"
    const val RENDER: String = "RENDER"
    const val ROTATE: String = "ROTATE"
    const val LOOKAT: String = "LOOKAT"
    const val LOOK_FROM_AT_O: String = "LOOK_FROM_AT_O"
    const val UNLOAD_MODEL: String = "UNLOAD_MODEL"
}
