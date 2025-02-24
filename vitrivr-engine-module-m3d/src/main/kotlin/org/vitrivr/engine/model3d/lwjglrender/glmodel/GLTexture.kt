package org.vitrivr.engine.model3d.lwjglrender.glmodel

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.vitrivr.engine.core.model.mesh.texturemodel.Texture

/**
 * The GLTexture class is a wrapper for the [Texture] class.
 * * Texture -> GLTexture( Texture )
 *
 * The purpose is to bring the generic Mesh in an OpenGl context [Texture] -> [GLTexture]
 */
class GLTexture(
    /** The wrapped generic texture in gl context */
    private val texture: Texture
) {
  /** The id of the texture used to bind the texture to the Gl context */
  private var textureId = 0

  /**
   * Creates a new GLTexture from a texture.
   * 1. Load the texture from the file
   * 1. Allocate the texture buffer
   * 1. Load the texture into the buffer
   *
   * @param texture The texture that is wrapped by this gl texture.
   */
  init {
    MemoryStack.stackPush().use { memoryStack ->
      val w = memoryStack.mallocInt(1)
      val h = memoryStack.mallocInt(1)
      val channels = memoryStack.mallocInt(1)
      val imageBuffer: ByteBuffer?
      if (this.texture.texturePath != null) {
        imageBuffer = STBImage.stbi_load(this.texture.texturePath!!, w, h, channels, 4)
      } else if (texture.textureImage != null) {
        val imagePath = "tmp.png"
        val outputFile = File(imagePath)
        try {
          // Write image to file
          ImageIO.write(texture.textureImage, "png", outputFile)
        } catch (e: IOException) {
          System.err.println("Error saving tmp texture image: " + e.message)
        }
        imageBuffer = STBImage.stbi_load(imagePath, w, h, channels, 4)
        if (outputFile.exists()) {
          // Attempt to delete the file
          outputFile.delete()
        }
      } else {
        throw RuntimeException("Could not load texture file: " + this.texture.texturePath)
      }
      this.generateTexture(w.get(), h.get(), imageBuffer)
      STBImage.stbi_image_free(imageBuffer!!)
    }
  }

  // TODO Convert the image data to a byte buffer to avoid tmp texture file --> requires further
  // debugging as the rendering afterward is not correct
  fun convertImageData(image: BufferedImage): ByteBuffer {
    val pixels = IntArray(image.width * image.height)
    image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)

    val buffer = BufferUtils.createByteBuffer(image.width * image.height * 3)

    buffer.clear()
    for (y in 0 until image.height) {
      for (x in 0 until image.width) {
        val pixel = pixels[y * image.width + x]
        buffer.put(((pixel shr 16) and 0xFF).toByte()) // Red component
        buffer.put(((pixel shr 8) and 0xFF).toByte()) // Green component
        buffer.put((pixel and 0xFF).toByte()) // Blue component
      }
    }
    buffer.clear()
    return buffer
  }

  /** Binds the GLTexture to the Gl context */
  fun bind() {
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId)
  }

  /**
   * Cleans the GLTexture Does not affect the underlying texture Removes the texture from the GPU
   */
  fun cleanup() {
    GL30.glDeleteTextures(this.textureId)
  }

  /**
   * Generates the texture in the Gl context
   *
   * @param width The width of the texture
   * @param height The height of the texture
   * @param texture The texture buffer
   */
  private fun generateTexture(width: Int, height: Int, texture: ByteBuffer?) {
    this.textureId = GL30.glGenTextures()
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId)
    GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1)
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST)
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST)
    GL30.glTexImage2D(
        GL30.GL_TEXTURE_2D,
        0,
        GL30.GL_RGBA,
        width,
        height,
        0,
        GL30.GL_RGBA,
        GL30.GL_UNSIGNED_BYTE,
        texture)
    GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D)
  }

  val texturePath: String?
    /**
     * Returns the texture path of the underlying wrapped texture
     *
     * @return The texture path of the underlying wrapped texture
     */
    get() = texture.texturePath

  val textureImage: BufferedImage?
    get() = texture.textureImage
}
