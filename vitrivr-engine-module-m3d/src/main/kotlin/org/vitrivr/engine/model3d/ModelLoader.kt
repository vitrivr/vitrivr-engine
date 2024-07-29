package org.vitrivr.engine.model3d

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.IntBuffer
import javax.imageio.ImageIO
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.assimp.*
import org.lwjgl.assimp.Assimp.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.vitrivr.engine.core.model.mesh.texturemodel.Material
import org.vitrivr.engine.core.model.mesh.texturemodel.Mesh
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.core.model.mesh.texturemodel.Texture
import java.io.InputStream
import java.nio.ByteBuffer

class ModelLoader {
  private val LOGGER: Logger = LogManager.getLogger()

  /**
   * Loads a model from a file. Generates all the standard flags for Assimp. For more details see
   * [Assimp](https://javadoc.lwjgl.org/org/lwjgl/assimp/Assimp.html).
   * * **aiProcess_GenSmoothNormals:** This is ignored if normals are already there at the time this
   *   flag is evaluated. Model importers try to load them from the source file, so they're usually
   *   already there. This flag may not be specified together with
   *
   * #aiProcess_GenNormals. There's a configuration option,
   * <tt>#AI_CONFIG_PP_GSN_MAX_SMOOTHING_ANGLE</tt> which allows you to specify an angle maximum for
   * the normal smoothing algorithm. Normals exceeding this limit are not smoothed, resulting in a
   * 'hard' seam between two faces. Using a decent angle here (e.g. 80 degrees) results in very good
   * visual appearance.
   * * **aiProcess_JoinIdenticalVertices:**
   * * **aiProcess_Triangulate** By default the imported mesh data might contain faces with more
   *   than 3 indices. For rendering you'll usually want all faces to be triangles. This post
   *   processing step splits up faces with more than 3 indices into triangles. Line and point
   *   primitives are *not* modified! If you want 'triangles only' with no other kinds of
   *   primitives, try the following solution:
   * * Specify both #aiProcess_Triangulate and #aiProcess_SortByPType Ignore all point and line
   *   meshes when you process assimp's output
   * * **aiProcess_FixInf acingNormals:** This step tries to determine which meshes have normal
   *   vectors that are facing inwards and inverts them. The algorithm is simple but effective: the
   *   bounding box of all vertices + their normals is compared against the volume of the bounding
   *   box of all vertices without their normals. This works well for most objects, problems might
   *   occur with planar surfaces. However, the step tries to filter such cases. The step inverts
   *   all in-facing normals. Generally it is recommended to enable this step, although the result
   *   is not always correct.
   * * **aiProcess_CalcTangentSpace:** Calculates the tangents and bi tangents for the imported
   *   meshes Does nothing if a mesh does not have normals. You might want this post processing step
   *   to be executed if you plan to use tangent space calculations such as normal mapping applied
   *   to the meshes. There's an importer property, AI_CONFIG_PP_CT_MAX_SMOOTHING_ANGLE, which
   *   allows you to specify a maximum smoothing angle for the algorithm. However, usually you'll
   *   want to leave it at the default value.
   * * **aiProcess_LimitBoneWeights:** Limits the number of bones simultaneously affecting a single
   *   vertex to a maximum value. If any vertex is affected by more than the maximum number of
   *   bones, the least important vertex weights are removed and the remaining vertex weights are
   *   normalized so that the weights still sum up to 1. The default bone weight limit is 4 (defined
   *   as AI_LBW_MAX_WEIGHTS in config.h), but you can use the AI_CONFIG_PP_LBW_MAX_WEIGHTS importer
   *   property to supply your own limit to the post processing step. If you intend to perform the
   *   skinning in hardware, this post processing step might be of interest to you.
   * * **aiProcess_PreTransformVertices:** Removes the node graph and pre-transforms all vertices
   *   with the local transformation matrices of their nodes. If the resulting scene can be reduced
   *   to a single mesh, with a single material, no lights, and no cameras, then the output scene
   *   will contain only a root node (with no children) that references the single mesh. Otherwise,
   *   the output scene will be reduced to a root node with a single level of child nodes, each one
   *   referencing one mesh, and each mesh referencing one material In either case, for rendering,
   *   you can simply render all meshes in order - you don't need to pay attention to local
   *   transformations and the node hierarchy. Animations are removed during this step This step is
   *   intended for applications without a scenegraph. The step CAN cause some problems: if e.g. a
   *   mesh of the asset contains normals and another, using the same material index, does not, they
   *   will be brought together, but the first mesh's part of the normal list is zeroed. However,
   *   these artifacts are rare.
   *
   * @param modelId The ID of the model.
   * @param modelPath Path to the model file.
   * @return Model object.
   */
  fun loadModel(modelId: String?, inputStream: InputStream): Model3d? {
    val model =
        loadModel(
            modelId,
            inputStream,
            aiProcess_JoinIdenticalVertices or
                aiProcess_GlobalScale or
                aiProcess_FixInfacingNormals or
                aiProcess_Triangulate or
                aiProcess_CalcTangentSpace or
                aiProcess_LimitBoneWeights or
                aiProcess_PreTransformVertices)
    LOGGER.trace("Try return Model 2")
    return model
  }

  // Keep function if we want to extend at a later point to load models again from path to get external textures
  fun loadModelPath(modelId: String?, path: String): Model3d? {
    val model =
      loadModel(
        modelId,
        path,
        aiProcess_JoinIdenticalVertices or
                aiProcess_GlobalScale or
                aiProcess_FixInfacingNormals or
                aiProcess_Triangulate or
                aiProcess_CalcTangentSpace or
                aiProcess_LimitBoneWeights or
                aiProcess_PreTransformVertices)
    LOGGER.trace("Try return Model 2")
    return model
  }

  /**
   * Loads a model from a file. 1. Loads the model file to an aiScene. 2. Process all Materials. 3.
   * Process all Meshes. 3.1 Process all Vertices. 3.2 Process all Normals. 3.3 Process all
   * Textures. 3.4 Process all Indices.
   *
   * @param modelId Arbitrary unique ID of the model.
   * @param inputStream InputStream where the model file is.
   * @param flags Flags for the model loading process.
   * @return Model object.
   */
  private fun loadModel(modelId: String?, inputStream: InputStream, flags: Int): Model3d? {
    LOGGER.trace("Try loading file {}.", modelId)

    val aiScene = modelId?.let { loadAIScene(it, inputStream) }

    /*val file = File(modelPath)
    if (!file.exists()) {
      throw RuntimeException("Model path does not exist [$modelPath]")
    }
    val modelDir = file.parent

    LOGGER.trace("Loading aiScene")*/

    // DO NOT USE AUTOCLOSEABLE TRY CATCH FOR AI-SCENE!!! THIS WILL CAUSE A FATAL ERROR ON NTH (199)
    // ITERATION!
    // RAPHAEL WALTENSPUEL 2023-01-20
    /*val aiScene =
        Assimp.aiImportFile(modelPath, flags)
            ?: throw RuntimeException("Error loading model [modelPath: $modelPath]")
*/
    val numMaterials = aiScene?.mNumMaterials()
    val materialList: MutableList<Material> = ArrayList()

    // TODO: Warning
    for (ic in 0 until numMaterials!!) {
      val aiMaterial = AIMaterial.create(aiScene.mMaterials()!![ic])
      LOGGER.trace("Try processing material {}", ic)
      materialList.add(processMaterial(aiMaterial, aiScene))
    }

    val numMeshes = aiScene.mNumMeshes()
    val aiMeshes = aiScene.mMeshes()
    val defaultMaterial = Material()
    for (ic in 0 until numMeshes) {
      LOGGER.trace("Try create AI Mesh {}", ic)
      // TODO: Warning
      val aiMesh = AIMesh.create(aiMeshes!![ic])
      val mesh = processMesh(aiMesh)
      LOGGER.trace("Try get Material idx")
      val materialIdx = aiMesh.mMaterialIndex()
      val material =
          if (materialIdx >= 0 && materialIdx < materialList.size) {
            materialList[materialIdx]
          } else {
            defaultMaterial
          }
      LOGGER.trace("Try add Material to Mesh")
      material.addMesh(mesh)
    }

    if (defaultMaterial.materialMeshes.isNotEmpty()) {
      LOGGER.trace("Try add default Material")
      materialList.add(defaultMaterial)
    }

    LOGGER.trace("Try instantiate Model")
    aiReleaseImport(aiScene)

    val model3d = modelId?.let { Model3d(it, materialList) }
    LOGGER.trace("Try return Model")
    return model3d
  }


  /**
   * Loads a model from a file. 1. Loads the model file to an aiScene. 2. Process all Materials. 3.
   * Process all Meshes. 3.1 Process all Vertices. 3.2 Process all Normals. 3.3 Process all
   * Textures. 3.4 Process all Indices.
   *
   * @param modelId Arbitrary unique ID of the model.
   * @param modelPath String to the model file.
   * @param flags Flags for the model loading process.
   * @return Model object.
   */
  fun loadModel(modelId: String?, modelPath: String, flags: Int): Model3d? {
    LOGGER.trace("Try loading file {}.", modelId)


    val file = File(modelPath)
    if (!file.exists()) {
      throw RuntimeException("Model path does not exist [$modelPath]")
    }

    LOGGER.trace("Loading aiScene")

    // DO NOT USE AUTOCLOSEABLE TRY CATCH FOR AI-SCENE!!! THIS WILL CAUSE A FATAL ERROR ON NTH (199)
    // ITERATION!
    // RAPHAEL WALTENSPUEL 2023-01-20
    val aiScene =
        aiImportFile(modelPath, flags)
            ?: throw RuntimeException("Error loading model [modelPath: $modelPath]")

    val numMaterials = aiScene.mNumMaterials()
    val materialList: MutableList<Material> = ArrayList()

    // TODO: Warning
    for (ic in 0 until numMaterials!!) {
      val aiMaterial = AIMaterial.create(aiScene.mMaterials()!![ic])
      LOGGER.trace("Try processing material {}", ic)
      materialList.add(processMaterial(aiMaterial, aiScene))
    }

    val numMeshes = aiScene.mNumMeshes()
    val aiMeshes = aiScene.mMeshes()
    val defaultMaterial = Material()
    for (ic in 0 until numMeshes) {
      LOGGER.trace("Try create AI Mesh {}", ic)
      // TODO: Warning
      val aiMesh = AIMesh.create(aiMeshes!![ic])
      val mesh = processMesh(aiMesh)
      LOGGER.trace("Try get Material idx")
      val materialIdx = aiMesh.mMaterialIndex()
      var material =
        if (materialIdx >= 0 && materialIdx < materialList.size) {
          materialList[materialIdx]
        } else {
          defaultMaterial
        }
      LOGGER.trace("Try add Material to Mesh")
      material.addMesh(mesh)
    }

    if (defaultMaterial.materialMeshes.isNotEmpty()) {
      LOGGER.trace("Try add default Material")
      materialList.add(defaultMaterial)
    }

    LOGGER.trace("Try instantiate Model")
    aiReleaseImport(aiScene)

    val model3d = modelId?.let { Model3d(it, materialList) }
    LOGGER.trace("Try return Model")
    return model3d
  }



  /**
   * Loads a AIScene from a file. Generates all the standard flags for Assimp. For more details see <a href="https://javadoc.lwjgl.org/org/lwjgl/assimp/Assimp.html">Assimp</a>.
   */
  private fun loadAIScene(modelId: String, inputStream: InputStream): AIScene {
    LOGGER.trace("Try loading model {} from InputStream", modelId)

    val data = inputStream.readBytes()
    val buffer = BufferUtils.createByteBuffer(data.size)
    buffer.put(data)
    buffer.flip()

    val aiScene = aiImportFileFromMemory(buffer, aiProcess_JoinIdenticalVertices or aiProcess_GlobalScale or aiProcess_FixInfacingNormals or aiProcess_Triangulate or aiProcess_CalcTangentSpace or aiProcess_LimitBoneWeights or aiProcess_PreTransformVertices, null as ByteBuffer?)
      ?: throw RuntimeException("Error loading model from InputStream")

    return aiScene
  }

  /**
   * Convert indices from aiMesh to int array.
   *
   * @param aiMesh aiMesh to process.
   * @return flattened int array of indices.
   */
  private fun processIndices(aiMesh: AIMesh): IntArray {
    LOGGER.trace("Start processing indices")
    val indices: MutableList<Int> = ArrayList()
    val numFaces = aiMesh.mNumFaces()
    val aiFaces = aiMesh.mFaces()
    for (ic in 0 until numFaces) {
      val aiFace = aiFaces[ic]
      val buffer = aiFace.mIndices()
      while (buffer.remaining() > 0) {
        indices.add(buffer.get())
      }
    }
    LOGGER.trace("End processing indices")
    return indices.stream().mapToInt { obj: Int -> obj }.toArray()
  }

  /**
   * Convert an AIMaterial to a Material. Loads the diffuse color and texture.
   *
   * @param aiMaterial aiMaterial to process.
   * @param modelDir Path to the model file.
   * @param aiScene AIScene to process.
   * @return flattened float array of vertices.
   */
  private fun processMaterial(
      aiMaterial: AIMaterial,
      aiScene: AIScene
  ): Material {
    LOGGER.trace("Start processing material")
    val material = Material()
    MemoryStack.stackPush().use { stack ->
      val color = AIColor4D.create()
      val result =
          aiGetMaterialColor(
              aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color)
      if (result == aiReturn_SUCCESS) {
        material.materialDiffuseColor = Vector4f(color.r(), color.g(), color.b(), color.a())
      }

      val aiTexturePath = AIString.calloc(stack)
      aiGetMaterialTexture(
          aiMaterial,
          aiTextureType_DIFFUSE,
          0,
          aiTexturePath,
          null as IntBuffer?,
          null,
          null,
          null,
          null,
          null)
      val texturePath = aiTexturePath.dataString()
      LOGGER.debug("Texture path: {}", texturePath)
      if (texturePath != null && texturePath.isNotEmpty()) {
        if (texturePath[0] == '*') {
          val textureIndex = texturePath.substring(1).toInt()
          LOGGER.debug("Embedded texture index: {}", textureIndex)
          val image = loadEmbeddedTexture(aiScene, textureIndex)
          if (image != null) {
            material.materialTexture = Texture(image)
            material.materialDiffuseColor = Material.DEFAULT_COLOR
          } else {
            LOGGER.error("Failed to load embedded texture at index {}", textureIndex)
          }
        } else {
          /*LOGGER.debug("Texture file path: {}", modelDir + File.separator + texturePath)
          material.materialTexture = Texture(modelDir + File.separator + File(texturePath).toPath())*/
          material.materialDiffuseColor = Material.DEFAULT_COLOR
        }
      } else {
        //LOGGER.warn("No texture path found for material")
      }
      return material
    }
  }

  /**
   * Convert aiMesh to a Mesh. Loads the vertices, normals, texture coordinates and indices.
   * Instantiates a new Mesh object.
   *
   * @param aiScene AIScene to process.
   * @return flattened float array of normals.
   */
  private fun loadEmbeddedTexture(aiScene: AIScene, textureIndex: Int): BufferedImage? {
    try {
      val aiTextures = aiScene.mTextures()
      if (aiTextures == null || textureIndex >= aiTextures.limit()) {
        LOGGER.error("No textures or invalid texture index {}", textureIndex)
        return null
      }

      val aiTexture = AITexture.create(aiTextures[textureIndex])
      if (aiTexture == null) {
        LOGGER.error("Failed to retrieve texture at index {}", textureIndex)
        return null
      }

      val address = aiTexture.pcData().address0()
      val buffer = MemoryUtil.memByteBuffer(address, aiTexture.mWidth())

      // Read the data into a byte array
      val texData = ByteArray(buffer.remaining())
      buffer[texData]

      // Create BufferedImage from decoded data
      val bis = ByteArrayInputStream(texData)
      val image = ImageIO.read(bis)
      bis.close()

      if (image != null) {
        LOGGER.debug("Successfully loaded embedded texture")
      } else {
        LOGGER.error("Failed to decode embedded texture to BufferedImage")
      }

      return image
    } catch (e: IOException) {
      LOGGER.error("Error loading embedded texture", e)
      return null
    } catch (e: Exception) {
      LOGGER.error("Error processing embedded texture", e)
      return null
    }
  }

  /**
   * Convert aiMesh to a Mesh. Loads the vertices, normals, texture coordinates and indices.
   * Instantiates a new Mesh object.
   *
   * @param aiMesh aiMesh to process.
   * @return flattened float array of normals.
   */
  private fun processMesh(aiMesh: AIMesh): Mesh {
    LOGGER.trace("Start processing mesh")
    val vertices = processVertices(aiMesh)
    val normals = processNormals(aiMesh)
    var textCoords = processTextCoords(aiMesh)
    val indices = processIndices(aiMesh)

    // Texture coordinates may not have been populated. We need at least the empty slots
    if (textCoords.isEmpty()) {
      val numElements = (vertices.size / 3) * 2
      textCoords = FloatArray(numElements)
    }
    LOGGER.trace("End processing mesh")
    return Mesh(vertices, normals, textCoords, indices)
  }

  /**
   * Convert normals from aiMesh to float array.
   *
   * @param aiMesh aiMesh to process.
   * @return flattened float array of normals.
   */
  private fun processNormals(aiMesh: AIMesh): FloatArray? {
    LOGGER.trace("Start processing Normals")
    val buffer = aiMesh.mNormals() ?: return null
    val data = FloatArray(buffer.remaining() * 3)
    var pos = 0
    while (buffer.remaining() > 0) {
      val normal = buffer.get()
      data[pos++] = normal.x()
      data[pos++] = normal.y()
      data[pos++] = normal.z()
    }
    return data
  }

  /**
   * Convert texture coordinates from aiMesh to float array.
   *
   * @param aiMesh aiMesh to process.
   * @return flattened float array of texture coordinates.
   */
  private fun processTextCoords(aiMesh: AIMesh): FloatArray {
    LOGGER.trace("Start processing Coordinates")
    val buffer = aiMesh.mTextureCoords(0) ?: return floatArrayOf()
    val data = FloatArray(buffer.remaining() * 2)
    var pos = 0
    while (buffer.remaining() > 0) {
      val textCoord = buffer.get()
      data[pos++] = textCoord.x()
      data[pos++] = 1 - textCoord.y()
    }
    return data
  }

  /**
   * Convert vertices from aiMesh to float array.
   *
   * @param aiMesh aiMesh to process.
   * @return flattened float array of vertices.
   */
  private fun processVertices(aiMesh: AIMesh): FloatArray {
    LOGGER.trace("Start processing Vertices")
    val buffer = aiMesh.mVertices()
    val data = FloatArray(buffer.remaining() * 3)
    var pos = 0
    while (buffer.remaining() > 0) {
      val textCoord = buffer.get()
      data[pos++] = textCoord.x()
      data[pos++] = textCoord.y()
      data[pos++] = textCoord.z()
    }

    return data
  }
}
