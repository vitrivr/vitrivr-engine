package org.vitrivr.engine.model3d.lwjglrender.renderer

/**
 * Actions used in the Render Workflow.
 * [RenderWorker]
 */
enum class RenderActions(val action: String) {
    SETUP("SETUP"),
    RENDER("RENDER"),
    ROTATE("ROTATE"),
    LOOKAT("LOOKAT"),
    LOOKAT_FROM("LOOKAT_FROM");
}
