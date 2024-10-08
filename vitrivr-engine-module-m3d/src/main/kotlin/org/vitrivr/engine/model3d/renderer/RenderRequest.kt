package org.vitrivr.engine.model3d.renderer

import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderActions
import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderData
import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderJob
import org.vitrivr.engine.model3d.lwjglrender.util.datatype.Variant
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import java.io.Serializable
import java.util.*
import java.util.concurrent.LinkedBlockingDeque

/**
 * A [RenderRequest] as processed by the [ExternalRenderer].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class RenderRequest(val model: Model3d, val cameraPositions: List<Vector3f>, val windowOptions: WindowOptions, val renderOptions: RenderOptions) : Serializable {


    companion object {
        private const val serialVersionUID: Long = 42L
    }

    /**
     * Converts this [RenderRequest] to a [RenderJob].
     *
     * @return [RenderJob]
     */
    fun toJob(): RenderJob {
        // Create data bag for the job.
        val jobData = Variant()
        jobData.set(RenderData.WINDOWS_OPTIONS, windowOptions)
            .set(RenderData.RENDER_OPTIONS, renderOptions)
            .set(RenderData.MODEL, model)

        // Setup the action sequence to perform the jop
        // In standard jop, this is an image for each camera position
        val actions = LinkedBlockingDeque<Action>()
        actions.add(Action(RenderActions.SETUP.name))
        actions.add(Action(RenderActions.SETUP.name))
        actions.add(Action(RenderActions.SETUP.name))

        val vectors = LinkedList<Vector3f>()
        for (position in cameraPositions) {
            // Create a copy of the vector to avoid concurrent modification exceptions
            vectors.add(Vector3f(position))
            actions.add(Action(RenderActions.LOOKAT_FROM.name))
            actions.add(Action(RenderActions.RENDER.name))
        }
        actions.add(Action(RenderActions.SETUP.name))
        jobData.set(RenderData.VECTORS, vectors)

        // Add the job to the queue
        return RenderJob(actions, jobData)
    }
}