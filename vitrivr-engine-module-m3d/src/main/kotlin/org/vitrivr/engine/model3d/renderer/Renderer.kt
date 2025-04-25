package org.vitrivr.engine.model3d.renderer

import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderData
import org.vitrivr.engine.model3d.lwjglrender.renderer.RenderWorker
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobType
import java.awt.image.BufferedImage
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import kotlin.system.exitProcess

/**
 * Main method of the external rendering process.
 */
fun main(args: Array<String>) {

    /* Initialize RenderWorker. */
    val worker = try {
        RenderWorker(LinkedBlockingDeque())
    } catch (e: Throwable) {
        System.err.println(e.message)
        exitProcess(1)
    }

    /* Obtain model path. */
    val path = args.getOrNull(0)?.let { Paths.get(it) }
    if (path == null || Files.notExists(path)) {
        System.err.println("No valid render request specified.")
        exitProcess(1)
    }

    /* Read render request. */
    val request = Files.newInputStream(path, StandardOpenOption.READ).use {
        ObjectInputStream(it).use { ois ->
            ois.readObject() as? RenderRequest
        }
    }
    if (request == null) {
        System.err.println("Could not parse model.")
        exitProcess(1)
    }

    /* Perform rendering. */
    val job = request.toJob()
    val images = LinkedList<BufferedImage>()

    /* Perform render job and collect images. */
    try {
        worker.performJob(job)
        var finished = false
        while (!finished) {
            val result = job.results
            when (result.type) {
                JobType.RESPONSE -> {
                    result.data?.get(BufferedImage::class.java, RenderData.IMAGE)?.run {
                        images.add(this)
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
        exitProcess(1)
    }

    /* Write images to file. */
    Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use {
        ObjectOutputStream(it).use { oos ->
            oos.writeObject(RenderResponse(images))
            oos.flush()
        }
    }

    /* End process. */
    exitProcess(0)
}