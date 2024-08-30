package org.vitrivr.engine.model3d.renderer

import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderData
import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderWorker
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobType
import java.awt.image.BufferedImage
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import kotlin.system.exitProcess

/**
 * Main method of the external rendering process.
 */
fun main() {

    /* Initialize RenderWorker. */
    val worker = try {
        RenderWorker(LinkedBlockingDeque())
    } catch (e: Throwable) {
        System.err.println(e.message)
        exitProcess(1)
    }

    /* Listen for incoming models. */
    ObjectOutputStream(System.out).use { os ->
        ObjectInputStream(System.`in`).use { ois ->
            while (true) {
                val received = ois.readObject() as? RenderRequest
                if (received == null) {
                    System.err.println("Could not parse model.")
                    os.writeObject(RenderResponse(emptyList()))
                    continue
                }

                /* Perform rendering. */
                val job = received.toJob()
                val images = LinkedList<BufferedImage>()

                /* Perform render job and collect images. */
                try {
                    worker.performJob(job)
                    var finished = false
                    while (!finished) {
                        val result = job.results
                        when (result.type) {
                            JobType.RESPONSE -> {
                                val result = result.data!!.get(BufferedImage::class.java, RenderData.IMAGE)
                                if (result is BufferedImage) {
                                    images.add(result)
                                }
                            }

                            JobType.CONTROL -> {
                                if (result.command == JobControlCommand.JOB_FAILURE) {
                                    System.err.println("Job failed.")
                                    finished = true
                                }
                                if (result.command == JobControlCommand.JOB_DONE) {
                                    finished = true
                                }
                            }

                            else -> {
                                /* No op. */
                            }
                        }
                    }
                } catch (e: Throwable) {
                    System.err.println("Unhandled error during processing: ${e.message}")
                } finally {
                    /* Send back response. */
                    os.writeObject(RenderResponse(images))
                    os.flush()
                }
            }
        }
    }
}