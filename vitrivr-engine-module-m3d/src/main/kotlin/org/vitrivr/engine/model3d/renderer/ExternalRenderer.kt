package org.vitrivr.engine.model3d.renderer

import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.model3d.data.render.lwjgl.render.RenderOptions
import org.vitrivr.engine.model3d.data.render.lwjgl.window.WindowOptions
import java.awt.image.BufferedImage
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * A helper class that boots an external renderer and allows to render [Model]s using it.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ExternalRenderer : Closeable {

    private val process: Process

    /** The [ObjectOutputStream] used by the [ExternalRenderer]. */
    private val oos: ObjectOutputStream

    /** The [ObjectInputStream] used by the [ExternalRenderer]. */
    private val ois: ObjectInputStream

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
     * Renders the given [Model3d] using the [ExternalRenderer].
     *
     * @param model The [Model3d] to render.
     * @param cameraPositions The [List] of [Vector3f] representing the camera positions.
     * @param windowOptions The [WindowOptions] to use for rendering.
     * @param renderOptions The [RenderOptions] to use for rendering.
     */
    fun render(model: Model3d, cameraPositions: List<Vector3f>, windowOptions: WindowOptions, renderOptions: RenderOptions): List<BufferedImage> {
        /* Create request and write it to stream. */
        val request = RenderRequest(model, cameraPositions, windowOptions, renderOptions)
        this.oos.writeObject(request)
        this.oos.flush()

        /* Read request. */
        val image = this.ois.readObject() as? RenderResponse ?: throw IllegalStateException("Could not parse model.")
        return image.images()
    }

    /**
     * Closes the [ExternalRenderer] and the associated process.
     */
    override fun close() {
        this.process.destroy()
    }
}