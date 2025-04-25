package org.vitrivr.engine.model3d

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.joml.Vector3f
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntopyCalculationMethod
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntropyOptimizerStrategy
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.OptimizerOptions
import org.vitrivr.engine.core.model.mesh.texturemodel.util.types.Vec3f
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.operators.general.ExporterFactory
import org.vitrivr.engine.core.resolver.Resolver
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.MimeType
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions
import org.vitrivr.engine.model3d.lwjglrender.util.texturemodel.entroopyoptimizer.ModelEntropyOptimizer
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import org.vitrivr.engine.model3d.renderer.ExternalRenderer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.*
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.MemoryCacheImageOutputStream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [Exporter] that generates a preview of a 3d model.
 *
 * @author Rahel Arnold
 * @version 2.1.0
 */
class ModelPreviewExporter : ExporterFactory {
    companion object {
        val SUPPORTED_INPUT = setOf(MimeType.GLTF, MimeType.GLB)

        /** Set of supported output formats. */
        val SUPPORTED_OUTPUT = setOf(MimeType.GIF, MimeType.JPG, MimeType.JPEG)

        /**
         * Renders a preview of the given model as a JPEG image.
         *
         * @param model The [Model3d] to render.
         * @param renderer The [ExternalRenderer] to use for rendering.
         * @param distance The distance of the camera from the model.
         * @return [BufferedImage]
         */
        fun renderPreviewJPEG(model: Model3d, renderer: ExternalRenderer, distance: Float = 1.0f): BufferedImage {
            if (model.modelMaterials.isNotEmpty()) {
                // Set options for the renderer.
                val windowOptions =
                    object : WindowOptions(400, 400) {
                        init {
                            hideWindow = true
                        }
                    }
                val renderOptions = RenderOptions(
                    showTextures = true
                )


                // Set options for the entropy optimizer.
                val opts = OptimizerOptions(
                    iterations = 100,
                    initialViewVector = Vec3f(0f, 0f, 1f),
                    method = EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA_WEIGHTED,
                    optimizer = EntropyOptimizerStrategy.RANDOMIZED,
                    yNegWeight = 0.7f,
                    yPosWeight = 0.8f
                )

                // Define camera positions
                val cameraPositions = LinkedList<Vector3f>()
                cameraPositions.add(
                    Vector3f(
                        (Math.random() - 0.5).toFloat() * 2f,
                        (Math.random() - 0.5).toFloat() * 2f,
                        (Math.random() - 0.5).toFloat() * 2f
                    )
                        .normalize()
                        .mul(distance)
                )
                cameraPositions.add(Vector3f(0f, 0f, 1f).normalize().mul(distance))
                cameraPositions.add(Vector3f(-1f, 1f, 1f).normalize().mul(distance))
                cameraPositions.add(ModelEntropyOptimizer.getViewVectorWithMaximizedEntropy(model, opts))

                /* Render the model. */
                val images = renderer.render(model, cameraPositions, windowOptions, renderOptions)
                require(images.size == 4) { "Expected 4 images, but got ${images.size}." }

                /* Combine images into a single image. */
                val combinedImage = BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB)
                val g = combinedImage.graphics
                g.drawImage(images[0], 0, 0, null) // Top-left
                g.drawImage(images[1], images[0].width, 0, null) // Top-right
                g.drawImage(images[2], 0, images[0].height, null) // Bottom-left
                g.drawImage(images[3], images[0].width, images[0].height, null)
                g.dispose()

                return combinedImage
            }
            throw IllegalArgumentException("Model has no materials.")
        }

        /**
         *
         */
        fun createFramesForGif(model: Model3d, renderer: ExternalRenderer, views: Int, distance: Float = 1.0f): List<BufferedImage> {
            if (model.modelMaterials.isNotEmpty()) {
                // Set options for the renderer.
                val windowOptions =
                    object : WindowOptions(400, 400) {
                        init {
                            hideWindow = true
                        }
                    }
                val renderOptions = RenderOptions(
                    showTextures = true
                )

                // Define camera positions depending on the number of views.
                val camera = generateCameraPositions(views, distance)
                val images = renderer.render(model, camera, windowOptions, renderOptions)

                assert(images.size == views)
                return images
            }
            throw IllegalArgumentException("Model has no materials.")
        }


        /**
         * Generates camera positions for a given number of views.
         */
        fun generateCameraPositions(numViews: Int, distance: Float): List<Vector3f> {
            val cameraPositions = LinkedList<Vector3f>()
            val goldenAngle = Math.PI * (3 - sqrt(5.0)) // Golden angle in radians

            for (i in 0 until numViews) {
                val y = 1 - (i / (numViews - 1.0)) * 2 // y goes from 1 to -1
                val radius = sqrt(1 - y * y) // radius at y

                val theta = goldenAngle * i // angle increment

                val x = (cos(theta) * radius).toFloat()
                val z = (sin(theta) * radius).toFloat()
                cameraPositions.add(Vector3f(x, y.toFloat(), z).normalize().mul(distance))
            }

            return cameraPositions
        }
    }

    /**
     * Creates a new [Exporter] instance from this [ModelPreviewExporter].
     *
     * @param name The name of the [Exporter]
     * @param input The [Operator] to acting as an input.
     * @param context The [IndexContext] to use.
     */
    override fun newExporter(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Exporter {
        val resolverName = context[name, "resolver"]?: "default"
        val maxSideResolution = context[name, "maxSideResolution"]?.toIntOrNull() ?: 800
        val mimeType =
            context[name, "mimeType"]?.let {
                try {
                    MimeType.valueOf(it.uppercase())
                } catch (e: java.lang.IllegalArgumentException) {
                    null
                }
            } ?: MimeType.GLTF
        val distance = context[name, "distance"]?.toFloatOrNull() ?: 1f
        val format = MimeType.valueOf(context[name, "format"]?.uppercase() ?: "GIF")
        val views = context[name, "views"]?.toIntOrNull() ?: 30
        logger.debug {
            "Creating new ModelPreviewExporter with maxSideResolution=$maxSideResolution and mimeType=$mimeType"
        }
        return Instance(input, context, resolverName, maxSideResolution, mimeType, distance, format, views, name)
    }

    /** The [Exporter] generated by this [ModelPreviewExporter]. */
    private class Instance(
        override val input: Operator<Retrievable>,
        private val context: IndexContext,
        resolverName: String,
        private val maxResolution: Int,
        mimeType: MimeType,
        private val distance: Float,
        private val format: MimeType,
        private val views: Int,
        override val name: String
    ) : Exporter {
        init {
            require(mimeType in SUPPORTED_INPUT) { "ModelPreviewExporter only supports models of format GLTF or GLB." }
            require(this.format in SUPPORTED_OUTPUT) { "ModelPreviewExporter only supports exporting a gif of jpg." }
        }

        /** [Resolver] instance. */
        private val resolver: Resolver = this.context.resolver[resolverName] ?: throw IllegalStateException("Unknown resolver with name $resolverName.")

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
            val renderer = ExternalRenderer()
            return this.input.toFlow(scope).onEach { retrievable ->
                val source =
                    retrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: return@onEach
                if (source.type == MediaType.MESH) {
                    val resolvable = this.resolver.resolve(retrievable.id, ".${this.format.fileExtension}")

                    val model = retrievable.content[0].content as Model3d
                    if (resolvable != null) {
                        logger.debug {
                            "Generating preview for ${retrievable.id} with ${retrievable.type} and resolution $maxResolution. Storing it with ${resolvable::class.simpleName}."
                        }

                        source.newInputStream().use { input ->
                            when (format) {
                                MimeType.JPG,
                                MimeType.JPEG -> {
                                    val preview: BufferedImage = renderPreviewJPEG(model, renderer, this.distance)
                                    resolvable.openOutputStream().use { output ->
                                        ImageIO.write(preview, "jpg", output)
                                    }
                                }
                                MimeType.GIF -> {
                                    val frames = createFramesForGif(model, renderer, this.views, this.distance)
                                    val gif = createGif(frames, 50)
                                    resolvable.openOutputStream().use { output -> output.write(gif!!.toByteArray()) }
                                }
                                else -> throw IllegalArgumentException("Unsupported mime type $format")
                            }
                        }
                    }
                }
            }
        }

        /**
         *
         */
        fun createGif(frames: List<BufferedImage>, delayTimeMs: Int): ByteArrayOutputStream? {
            if (frames.isEmpty()) {
                println("No frames to write to GIF.")
                return null
            }

            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val outputImageStream = MemoryCacheImageOutputStream(byteArrayOutputStream)
                val gifWriter: ImageWriter? = ImageIO.getImageWritersBySuffix("gif").next()

                if (gifWriter != null) {
                    gifWriter.output = outputImageStream
                    gifWriter.prepareWriteSequence(null)

                    val param: ImageWriteParam = gifWriter.defaultWriteParam
                    param.compressionMode = ImageWriteParam.MODE_EXPLICIT

                    val delayTime = (delayTimeMs).toString()

                    for (frame in frames) {
                        val image = IIOImage(frame, null, getMetadata(gifWriter, delayTime))
                        gifWriter.writeToSequence(image, param)
                    }

                    gifWriter.endWriteSequence()
                    outputImageStream.close()
                    gifWriter.dispose()

                    // println("GIF created successfully.")
                    return byteArrayOutputStream
                } else {
                    println("Failed to create GIF writer.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun getMetadata(gifWriter: ImageWriter, delayTime: String): IIOMetadata {
            val imageType = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)
            val metadata = gifWriter.getDefaultImageMetadata(imageType, null)
            val metaFormatName = metadata.nativeMetadataFormatName

            val root = IIOMetadataNode(metaFormatName)
            val graphicsControlExtensionNode = IIOMetadataNode("GraphicControlExtension")

            graphicsControlExtensionNode.setAttribute("disposalMethod", "none")
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE")
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE")
            graphicsControlExtensionNode.setAttribute("delayTime", delayTime)
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0")

            root.appendChild(graphicsControlExtensionNode)

            val appExtensionsNode = IIOMetadataNode("ApplicationExtensions")
            val appExtensionNode = IIOMetadataNode("ApplicationExtension")

            appExtensionNode.setAttribute("applicationID", "NETSCAPE")
            appExtensionNode.setAttribute("authenticationCode", "2.0")
            appExtensionNode.userObject = byteArrayOf(0x1, 0x0, 0x0)

            appExtensionsNode.appendChild(appExtensionNode)
            root.appendChild(appExtensionsNode)

            metadata.mergeTree(metaFormatName, root)

            return metadata
        }
    }
}
