package org.vitrivr.engine.model3d.lwjglrender.glmodel

import java.util.function.Consumer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.vitrivr.engine.core.model.mesh.texturemodel.Mesh

/**
 * The GLMesh class is a wrapper for the [Mesh] class.
 * * Mesh -> GLMesh( Mesh )
 *
 * The purpose is to bring the generic Mesh in an OpenGl context [Mesh] -> [GLMesh]
 */
class GLMesh(
    /** The wrapped generic mesh in gl context */
    private val mesh: Mesh
) {
  /** The list of *Vertex Buffer Object* (VBO) ids */
  private val vboIdList: MutableList<Int> = ArrayList()
  /**
   * Returns the *Vertex Array Object* (VAO) id.
   *
   * @return The *Vertex Array Object* (VAO) id.
   */
  /** The *Vertex Array Object* (VAO) id */
  var vaoId: Int = 0

  /**
   * Creates a new GLMesh from a mesh.
   * 1. Bind Vertex Array Object
   * 1. Generate, allocate and initialize Vertex (Positions) Buffer
   * 1. Generate, allocate and initialize Texture Coordinates Buffer
   * 1. Generate, allocate and initialize Index Buffer
   * 1. Unbind Vertex Array Object
   *
   * @param mesh The mesh that is wrapped by this gl mesh.
   */
  init {
    MemoryStack.stackPush().use { memoryStack ->
      this.vaoId = GL30.glGenVertexArrays()
      GL30.glBindVertexArray(this.vaoId)

      // Positions VBO
      var vboId = GL30.glGenBuffers()
      vboIdList.add(vboId)
      val positionsBuffer = memoryStack.callocFloat(mesh.getPositions().size)
      positionsBuffer.put(0, mesh.getPositions())
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, positionsBuffer, GL30.GL_STATIC_DRAW)
      GL30.glEnableVertexAttribArray(0)
      GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0)

      // Textures VBO (Vertex Buffer Object)
      vboId = GL30.glGenBuffers()
      vboIdList.add(vboId)
      val textureCoordinatesBuffer = MemoryUtil.memAllocFloat(mesh.getTextureCoords().size)
      textureCoordinatesBuffer.put(0, mesh.getTextureCoords())
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textureCoordinatesBuffer, GL30.GL_STATIC_DRAW)
      GL30.glEnableVertexAttribArray(1)
      GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0)

      // Index VBO (Vertex Buffer Object)
      vboId = GL30.glGenBuffers()
      vboIdList.add(vboId)
      val idxBuffer = memoryStack.callocInt(mesh.getIdx().size)
      idxBuffer.put(0, mesh.getIdx())
      GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, vboId)
      GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL30.GL_STATIC_DRAW)

      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
      GL30.glBindVertexArray(0)
    }
  }

  /**
   * Cleans up the gl mesh and calls all underlying cleanup methods. Removes only the references to
   * VBOs and VAOs. Removes the *Vertex Array Object* (VAO) and all *Vertex Buffer Object* (VBO)
   * ids.
   */
  fun cleanup() {
    vboIdList.forEach(Consumer { buffer: Int? -> GL30.glDeleteBuffers(buffer!!) })
    GL30.glDeleteVertexArrays(this.vaoId)
    vboIdList.clear()
    LOGGER.trace("Cleaned-up GLMesh")
  }

  val numVertices: Int
    /**
     * Returns the number of vertices of the wrapped generic mesh.
     *
     * @return The number of vertices of the wrapped generic mesh.
     */
    get() = mesh.getNumVertices()

  val id: String?
    /**
     * Returns the ID of the wrapped generic mesh.
     *
     * @return The ID of the wrapped generic mesh.
     */
    get() = mesh.id

  companion object {
    private val LOGGER: Logger = LogManager.getLogger()
  }
}
