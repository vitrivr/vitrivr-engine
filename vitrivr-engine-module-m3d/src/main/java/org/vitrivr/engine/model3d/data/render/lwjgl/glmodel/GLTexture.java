package org.vitrivr.engine.model3d.data.render.lwjgl.glmodel;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.vitrivr.engine.model3d.data.texturemodel.Texture;

import javax.imageio.ImageIO;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The GLTexture class is a wrapper for the {@link Texture} class.
 * <ul>
 * <li>Texture -> GLTexture( Texture )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic Mesh in an OpenGl context
 * {@link Texture} -> {@link GLTexture}
 */
public class GLTexture {

  /**
   * The id of the texture used to bind the texture to the Gl context
   */
  private int textureId;
  /**
   * The wrapped generic texture in gl context
   */
  private final Texture texture;

  /**
   * Creates a new GLTexture from a texture.
   * <ol>
   *   <li>Load the texture from the file</li>
   *   <li>Allocate the texture buffer</li>
   *   <li>Load the texture into the buffer</li>
   * </ol>
   * @param texture The texture that is wrapped by this gl texture.
   */
  public GLTexture(Texture texture) {
    this.texture = texture;
    try (var memoryStack = MemoryStack.stackPush()) {
      var w = memoryStack.mallocInt(1);
      var h = memoryStack.mallocInt(1);
      var channels = memoryStack.mallocInt(1);
      ByteBuffer imageBuffer;
      if (this.texture.getTexturePath()!= null){
        imageBuffer = STBImage.stbi_load(this.texture.getTexturePath(), w, h, channels, 4);
      } else if (this.texture.getTextureImage() != null) {
        String imagePath = "tmp.png";
        File outputFile = new File(imagePath);
        try {
          // Write image to file
          ImageIO.write(this.texture.getTextureImage(), "png", outputFile);
        } catch (IOException e) {
          System.err.println("Error saving tmp texture image: " + e.getMessage());
        }
        imageBuffer = STBImage.stbi_load(imagePath, w, h, channels, 4);
        if (outputFile.exists()) {
          // Attempt to delete the file
         outputFile.delete();
        };
      } else {
        throw new RuntimeException("Could not load texture file: " + this.texture.getTexturePath());
      }
      this.generateTexture(w.get(), h.get(), imageBuffer);
      STBImage.stbi_image_free(imageBuffer);
    }
  }

  // TODO Convert the image data to a byte buffer to avoid tmp texture file --> requires further debugging as the rendering afterward is not correct
  public ByteBuffer convertImageData(BufferedImage image) {

    int[] pixels = new int[image.getWidth() * image.getHeight()];
    image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

    ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 3);

    buffer.clear() ;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int pixel = pixels[y * image.getWidth() + x];
        buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
        buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
        buffer.put((byte) (pixel & 0xFF)); // Blue component
      }
    }
    buffer.clear();
    return buffer;
  }

  /**
   * Binds the GLTexture to the Gl context
   */
  public void bind() {
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
  }

  /**
   * Cleans the GLTexture
   * Does not affect the underlying texture
   * Removes the texture from the GPU
   */
  public void cleanup() {
    GL30.glDeleteTextures(this.textureId);
  }

  /**
   * Generates the texture in the Gl context
   * @param width The width of the texture
   * @param height The height of the texture
   * @param texture The texture buffer
   */
  private void generateTexture(int width, int height, ByteBuffer texture) {
    this.textureId = GL30.glGenTextures();
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
    GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
    GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE,
        texture);
    GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);
  }

  /**
   * Returns the texture path of the underlying wrapped texture
   * @return The texture path of the underlying wrapped texture
   */
  public String getTexturePath() {
    return this.texture.getTexturePath();
  }

  public BufferedImage getTextureImage() {
    return this.texture.getTextureImage();
  }

}

