package org.vitrivr.engine.model3d.data.render.lwjgl.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL30;


/**
 * ShaderProgram
 * Loads a shader program from the resources to the GL context
 */
public class ShaderProgram {

  /**
   * Shader Program ID, is used to bind and release the program from the GL context
   */
  private final int programId;

  /**
   * Creates a new ShaderProgram
   * Takes a list of ShaderModuleData (usually from Scene Renderer which loads the shaders from the resources during construction
   * Creates a new ShaderProgram in GL context, links the shaders and validates the program
   * For Shader creation, the following steps are performed:
   * <ul>
   *   <li>Reads the shader file</li>
   *   <li>Creates a new shader in the GL context {@link ShaderProgram#createShader(String, int)}</li>
   *   <li>Compiles the shader</li>
   *   <li>Attaches the shader to the program</li>
   *   <li>Links the program</li>
    *   <li>Binds the program to the GL context</li>
   *  </ul>
   * @param shaderModuleDataList List of ShaderModuleData
   */
  public ShaderProgram(List<ShaderModuleData> shaderModuleDataList) {
    this.programId = GL30.glCreateProgram();
    if (this.programId == 0) {
      throw new RuntimeException("Could not create shader.");
    }
    var shaderModules = new ArrayList<Integer>();
    shaderModuleDataList.forEach(s -> shaderModules.add(this.createShader(ShaderProgram.readShaderFile(s.shaderFile), s.shaderType)));
    this.link(shaderModules);
  }

  /**
   * Binds the ShaderProgram to the GL context
   */
  public void bind() {
    GL30.glUseProgram(this.programId);
  }

  /**
   * Unbinds the ShaderProgram from the GL context
   */
  public void unbind() {
    GL30.glUseProgram(0);
  }

  /**
   * Unbinds the ShaderProgram from the GL context
   * Deletes the ShaderProgram from the GL context
   */
  public void cleanup() {
    this.unbind();
    if (this.programId != 0) {
      GL30.glDeleteProgram(this.programId);
    }
  }

  /**
   * Reads the shader file.
   *
   * @param filePath Path to the shader file
   * @return String containing the shader code
   */
  public static String readShaderFile(String filePath) {
    String shader = null;

    /* First try: Load from resources. */
    try (final InputStream stream = ShaderProgram.class.getResourceAsStream(filePath)) {
      if (stream != null) {
        shader = new String(stream.readAllBytes());
      }
    } catch (IOException e) {
      /* No op. */
    }

    /* Second attempt try: Load from resources. */
    if (shader == null) {
      try {
        shader = new String(Files.readAllBytes(Paths.get(filePath)));
      } catch (IOException ex) {
        /* No op. */
      }
    }

    /* Make sure shader has been loaded. */
    if (shader == null) {
      throw new IllegalStateException("Error reading shader file: " + filePath);
    }
    return shader;
  }

  /**
   * Links the program
   * Deletes the shaders
   * @param shaderModules List of shader ids
   */
  private void link(List<Integer> shaderModules) {
    GL30.glLinkProgram(this.programId);
    if (GL30.glGetProgrami(this.programId, GL30.GL_LINK_STATUS) == 0) {
      throw new RuntimeException("Error linking Shader");
    }
    shaderModules.forEach(s -> GL30.glDetachShader(this.programId, s));
    shaderModules.forEach(GL30::glDeleteShader);
    //this.validate();
  }

  /**
   * Validates the program
   * Throws an exception if the program is not valid
   */
  public void validate() {
    GL30.glValidateProgram(this.programId);
    if (GL30.glGetProgrami(this.programId, GL30.GL_VALIDATE_STATUS) == 0) {
      throw new RuntimeException("Error validate Shader");
    }
  }

  /**
   * Returns the program id
   * @return program id
   */
  public int getProgramId() {
    return this.programId;
  }

  /**
   * Creates a new Shader in the GL context
   * Compiles the shader
   * Attaches the shader to the program
   * @return the shader id
   */
  protected int createShader(String shaderCode, int shaderType) {
    int shaderId = GL30.glCreateShader(shaderType);
    if (shaderId == 0) {
      throw new IllegalStateException("Error creating shader");
    }
    GL30.glShaderSource(shaderId, shaderCode);
    GL30.glCompileShader(shaderId);

    if (GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS) == 0) {
      throw new IllegalStateException("Error compiling shader");
    }
    GL30.glAttachShader(this.programId, shaderId);
    return shaderId;
  }

  /**
   * RECORD for ShaderModuleData
   * @param shaderFile
   * @param shaderType
   */
  public record ShaderModuleData(String shaderFile, int shaderType) {
  }
}
