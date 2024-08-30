package org.vitrivr.engine.model3d.lwjglrender.scene

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import java.awt.image.BufferedImage
import java.nio.FloatBuffer

/**
 * The LightfieldCamera class is used to take a picture of the current rendered scene.
 * The picture is stored as a BufferedImage.
 */
class LightfieldCamera(
    /**
     * The WindowOptions class is used to set the width and height of the resulting image.
     */
    private val opts: WindowOptions
) {
    /**
     * The BufferedImage that is used to store the image data.
     */
    private val lightfieldImage = BufferedImage(opts.width, opts.height, BufferedImage.TYPE_INT_RGB)

    /**
     * The FloatBuffer from openGL that holds the image data.
     */
    private val imageData: FloatBuffer = BufferUtils.createFloatBuffer(opts.width * opts.height * 3)


    /**
     * Initializes the LightfieldCamera with the given WindowOptions.
     * Creates a new BufferedImage with the given width and height.
     * Reads the image data from the current openGL context.
     * @param opts The WindowOptions that are used to set the width and height of the resulting image.
     */
    init {
        GL30.glReadPixels(0, 0, opts.width, opts.height, GL30.GL_RGB, GL30.GL_FLOAT, this.imageData)
        imageData.rewind()
    }

    /**
     * Takes a picture of the current rendered scene.
     * Updates the image data of the BufferedImage.
     * Returns the image data as a BufferedImage.
     * @return The RenderedScene as a BufferedImage.
     */
    fun takeLightfieldImage(): BufferedImage {
        this.takePicture()
        return this.lightfieldImage
    }

    /**
     * This method start calculating the pixels of the BufferedImage.
     */
    private fun takePicture() {
        lightfieldImage.setRGB(0, 0, opts.width, opts.height, this.rgbData, 0, opts.width)
    }


    private val rgbData: IntArray
        /**
         * This method converts the pixels of the BufferedImage.
         * R, G, B values are merged into one int value.
         * E.g.
         * <pre>
         * R = 0xAA -> AA0000, G = 0xBB -> 0x00BB00, B = 0xCC -> 0x0000CC
         * R+ G + B = 0xAABBCC
        </pre> *
         *
         * @return The image data as an int array.
         */
        get() {
            val rgbArray = IntArray(opts.height * opts.width)

            for (y in 0 until opts.height) {
                for (x in 0 until opts.width) {
                    val r = (imageData.get() * 255).toInt() shl 16
                    val g = (imageData.get() * 255).toInt() shl 8
                    val b = (imageData.get() * 255).toInt()
                    val i = ((opts.height - 1) - y) * opts.width + x
                    rgbArray[i] = r + g + b
                }
            }
            return rgbArray
        }
}