package org.vitrivr.engine.model3d.lwjglrender.renderer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel
import org.vitrivr.engine.model3d.lwjglrender.util.datatype.Variant
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.Job
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobType
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

/**
 * The RenderJob is a job which is  responsible for rendering a model.
 *
 *
 * This job extends the abstract class Job.
 *
 *
 * It provides constructors for the different types of jobs.
 * ORDER Job to render a model.
 * COMMAND Job signals caller that the job is done or an error occurred.
 * RESULT Job contains the result of the rendering.
 */
class RenderJob : Job {
    /**
     * Creates a new ORDER RenderJob with the given action sequence and data (containing the model to render).
     */
    constructor(actions: BlockingDeque<Action>?, data: Variant?) : super(actions, data)

    /**
     * Creates a new RESPONSE RenderJob with the rendered image.
     */
    constructor(data: Variant?) : super(data)

    /**
     * Creates a new CONTROL RenderJob with the given command.
     */
    constructor(command: JobControlCommand?) : super(command)


    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /**
         * Static method to create a standard render job.
         *
         *
         */
        fun performStandardRenderJob(
            renderJobQueue: BlockingDeque<RenderJob?>,
            model: IModel?,
            cameraPositions: Array<DoubleArray>,
            windowOptions: WindowOptions?,
            renderOptions: RenderOptions?
        ): List<BufferedImage> {
            val cameraPositionVectors = LinkedList<Vector3f>()
            for (cameraPosition in cameraPositions) {
                assert(cameraPosition.size == 3)
                cameraPositionVectors.add(
                    Vector3f(
                        cameraPosition[0].toFloat(),
                        cameraPosition[1].toFloat(),
                        cameraPosition[2].toFloat()
                    )
                )
            }
            return performStandardRenderJob(renderJobQueue, model!!, cameraPositionVectors, windowOptions!!, renderOptions!!)
        }

        /**
         * Static method to create a standard render job.
         *
         *
         *
         *  * Creates a job for given model and each camera position.
         *  * Adds the data to the variant (data bag)
         *  * Generates the needed actions for the job.
         *  * Creates the job and adds it to the provided queue.
         *  * Waits for the job to finish. (or fail)
         *  * Returns the rendered images.
         *  * Cleans up the resources.
         *
         *
         *
         * @param renderJobQueue The queue to add the job to.
         * @param model        The model to render.
         * @param cameraPositions The camera positions to render the model from.
         * @param windowOptions The window options to use for the rendering.
         * @param renderOptions The render options to use for the rendering.
         * @return The rendered images.
         */
        fun performStandardRenderJob(
            renderJobQueue: BlockingDeque<RenderJob?>,
            model: IModel,
            cameraPositions: LinkedList<Vector3f>,
            windowOptions: WindowOptions,
            renderOptions: RenderOptions
        ): List<BufferedImage> {
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
            val job = RenderJob(actions, jobData)
            renderJobQueue.add(job)

            // Wait for the job to finish
            var finishedJob = false
            val image = ArrayList<BufferedImage>()

            // Add images to result or finish the job
            try {
                while (!finishedJob) {
                    val result = job.results
                    if (result.type == JobType.RESPONSE) {
                        image.add(result.data!!.get(BufferedImage::class.java, RenderData.IMAGE))
                    } else if (result.type == JobType.CONTROL) {
                        if (result.command == JobControlCommand.JOB_DONE) {
                            finishedJob = true
                        }
                        if (result.command == JobControlCommand.JOB_FAILURE) {
                            LOGGER.error("Job failed")
                            finishedJob = true
                        }
                    }
                }
            } catch (ex: InterruptedException) {
                LOGGER.error("Could not render model", ex)
            } finally {
                job.clean()
            }
            return image
        }
    }
}