package org.vitrivr.engine.model3d.renderer

import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import java.awt.image.BufferedImage
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

/**
 * A helper class that boots an external renderer and allows to render 3D models using it.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
class ExternalRenderer {
    companion object {
        private const val CLASS_NAME = "org.vitrivr.engine.model3d.renderer.RendererKt"
    }

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
        /* Prepare request and store it in temporary file. */
        val request = RenderRequest(model, cameraPositions, windowOptions, renderOptions)
        val tmp = Files.createTempFile("vitrivr-renderer-", ".tmp")
        Files.newOutputStream(tmp,  StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { out ->
            ObjectOutputStream(out).use { oos ->
                oos.writeObject(request)
                oos.flush()
            }
        }

        /* Create render process and send request. */
        val process = this.startProcess(path = tmp)
        if (process.waitFor(10_000, TimeUnit.MILLISECONDS)) {
            if (process.exitValue() != 0) throw IllegalStateException("External renderer exited with error code ${process.exitValue()}.")
            val response = Files.newInputStream(tmp, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE).use {
                ObjectInputStream(it).use { ois ->
                    ois.readObject() as? RenderResponse ?: throw IllegalStateException("Could not parse model.")
                }
            }
            return response.images()
        }

        /* Process did not terminate in time. */
        return emptyList()
    }

    /**
     * Starts the external renderer [Process].
     *
     * @param path The [Path] to the temporary file.
     * @return The [Process] of the external renderer.
     */
    private fun startProcess(path: Path): Process {
        val javaBin = ProcessHandle.current().info().command().getOrNull() ?: throw IllegalStateException("Could not determine JAVA_HOME.")
        val classpath = System.getProperty("java.class.path")
        val os = System.getProperty("os.name").lowercase()
        val processBuilder = if (os.contains("mac")) {
            ProcessBuilder(javaBin, "-cp", classpath, "-XstartOnFirstThread", CLASS_NAME, path.toString()) /* Mac only issue. */
        } else {
            ProcessBuilder(javaBin, "-cp", classpath, CLASS_NAME, path.toString())
        }
        if (os.contains("linux")) {
            processBuilder.environment()["DISPLAY"] = ":1"
        }
        return processBuilder.start()
    }
}