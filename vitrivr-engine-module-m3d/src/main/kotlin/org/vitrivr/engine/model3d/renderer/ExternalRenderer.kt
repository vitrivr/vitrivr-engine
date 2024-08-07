package org.vitrivr.engine.model3d.renderer

import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import java.awt.image.BufferedImage
import java.io.Closeable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * A helper class that boots an external renderer and allows to render [Model]s using it.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ExternalRenderer : Closeable {

    private var process: RenderProcess? = null

    @Volatile
    /** Flag indicating whether the [ExternalRenderer] is closed. */
    private var closed: Boolean = false

    /**
     * Renders the given [Model3d] using the [ExternalRenderer].
     *
     * @param model The [Model3d] to render.
     * @param cameraPositions The [List] of [Vector3f] representing the camera positions.
     * @param windowOptions The [WindowOptions] to use for rendering.
     * @param renderOptions The [RenderOptions] to use for rendering.
     */
    @Synchronized
    fun render(model: Model3d, cameraPositions: List<Vector3f>, windowOptions: WindowOptions, renderOptions: RenderOptions): List<BufferedImage> {
        check(!this.closed) { "ExternalRenderer is closed and cannot be used for processing." }
        var process = this.process
        if (process == null || !process.isAlive()) {
            process?.close()
            process = RenderProcess()
            this.process = process
        }

        /* Send request. */
        val request = RenderRequest(model, cameraPositions, windowOptions, renderOptions)
        val response = process.send(request)

        /* Return images . */
        return response.images()
    }

    /**
     * Closes the [ExternalRenderer] and the associated process.
     */
    override fun close() {
        if (!this.closed) {
            this.closed = true
            this.process?.close()
            this.process = null
        }
    }

    /**
     * A [RenderProcess] is a helper class that wraps the [Process] used by the [ExternalRenderer].
     */
    private inner class RenderProcess: Closeable {

        /** The [Process] used by the [ExternalRenderer]. */
        val process: Process

        /** The [ObjectOutputStream] used by the [ExternalRenderer]. */
        val oos: ObjectOutputStream

        /** The [ObjectInputStream] used by the [ExternalRenderer]. */
        val ois: ObjectInputStream

        init {
            val javaHome = System.getProperty("java.home")
            val javaBin = "$javaHome/bin/java"
            val classpath = System.getProperty("java.class.path")
            val className = "org.vitrivr.engine.model3d.renderer.RendererKt"

            val processBuilder = ProcessBuilder(javaBin, "-cp", classpath, "-XstartOnFirstThread", className)
            this.process = processBuilder.start()

            /* Initialize streams. */
            this.oos = ObjectOutputStream(this.process.outputStream)
            this.ois = ObjectInputStream(this.process.inputStream)
        }

        /**
         * Sends a [RenderRequest] to the external renderer.
         */
        fun send(request: RenderRequest): RenderResponse {
            /* Write request to output stream. */
            try {
                this.oos.writeObject(request)
                this.oos.flush()
            } catch (e: IOException) {
                this.oos.reset()
                throw IllegalStateException("Could not send request due to IO exception.", e)
            }

            /* Read response and return image. */
            val image = try {
                this.ois.readObject() as? RenderResponse ?: throw IllegalStateException("Could not parse model.")
            } catch (e: IOException) {
                this.ois.reset()
                throw IllegalStateException("Could not parse model due to IO exception.", e)
            }
            return image
        }

        /**
         * Checks if the process is still alive.
         */
        fun isAlive(): Boolean {
            return this.process.isAlive
        }

        /**
         * Checks if the process is still alive.
         */
        override fun close() = try {
            this.oos.close()
            this.ois.close()
        } finally {
            this.process.destroy()
        }
    }
}