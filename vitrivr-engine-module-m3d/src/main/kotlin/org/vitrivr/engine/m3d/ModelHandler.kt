package org.vitrivr.engine.m3d


import org.apache.logging.log4j.LogManager
import org.joml.Vector4f
import org.lwjgl.assimp.*
import org.lwjgl.assimp.Assimp.*

import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.IntBuffer
import java.util.*
import kotlin.collections.ArrayList


class ModelHandler {
    private val LOGGER = LogManager.getLogger()


    /**
     * Loads a model from a file. Generates all the standard flags for Assimp. For more details see <a href="https://javadoc.lwjgl.org/org/lwjgl/assimp/Assimp.html">Assimp</a>.
     * <ul>
     *   <li><b>aiProcess_GenSmoothNormals:</b>
     *        This is ignored if normals are already there at the time this flag
     *       is evaluated. Model importers try to load them from the source file, so
     *       they're usually already there.
     *       This flag may not be specified together with
     *       #aiProcess_GenNormals. There's a configuration option,
     *       <tt>#AI_CONFIG_PP_GSN_MAX_SMOOTHING_ANGLE</tt> which allows you to specify
     *       an angle maximum for the normal smoothing algorithm. Normals exceeding
     *       this limit are not smoothed, resulting in a 'hard' seam between two faces.
     *       Using a decent angle here (e.g. 80 degrees) results in very good visual
     *       appearance.
     *       </li>
     *   <li><b>aiProcess_JoinIdenticalVertices:</b></li>
     *   <li><b>aiProcess_Triangulate</b> By default the imported mesh data might contain faces with more than 3
     *        indices. For rendering you'll usually want all faces to be triangles.
     *        This post processing step splits up faces with more than 3 indices into
     *        triangles. Line and point primitives are *not* modified! If you want
     *        'triangles only' with no other kinds of primitives, try the following
     *        solution:
     *        <ul>
     *          <li>Specify both #aiProcess_Triangulate and #aiProcess_SortByPType </li>
     *          </li>Ignore all point and line meshes when you process assimp's output</li>
     *        </ul>
     *   </li>
     *   <li><b>aiProcess_FixInf acingNormals:</b>
     *        This step tries to determine which meshes have normal vectors that are facing inwards and inverts them.
     *        The algorithm is simple but effective: the bounding box of all vertices + their normals is compared against
     *        the volume of the bounding box of all vertices without their normals. This works well for most objects, problems might occur with
     *        planar surfaces. However, the step tries to filter such cases.
     *        The step inverts all in-facing normals. Generally it is recommended to enable this step, although the result is not always correct.
     *   </li>
     *   <li><b>aiProcess_CalcTangentSpace:</b>
     *        Calculates the tangents and bi tangents for the imported meshes
     *        Does nothing if a mesh does not have normals.
     *        You might want this post processing step to be executed if you plan to use tangent space calculations such as normal mapping applied to the meshes.
     *        There's an importer property, AI_CONFIG_PP_CT_MAX_SMOOTHING_ANGLE, which allows you to specify a maximum smoothing angle for the algorithm.
     *        However, usually you'll want to leave it at the default value.
     *   </li>
     *   <li><b>aiProcess_LimitBoneWeights:</b>
     *    Limits the number of bones simultaneously affecting a single vertex to a maximum value.
     *    If any vertex is affected by more than the maximum number of bones,
     *    the least important vertex weights are removed and the remaining vertex weights are normalized so that the weights still sum up to 1.
     *    The default bone weight limit is 4 (defined as AI_LBW_MAX_WEIGHTS in config.h),
     *    but you can use the AI_CONFIG_PP_LBW_MAX_WEIGHTS importer property to supply your own limit to the post processing step.
     *    If you intend to perform the skinning in hardware, this post processing step might be of interest to you.
     *   </li>
     *   <li><b>aiProcess_PreTransformVertices:</b>
     *    Removes the node graph and pre-transforms all vertices with the local transformation matrices of their nodes.
     *    If the resulting scene can be reduced to a single mesh, with a single material, no lights, and no cameras,
     *    then the output scene will contain only a root node (with no children) that references the single mesh.
     *    Otherwise, the output scene will be reduced to a root node with a single level of child nodes, each one referencing one mesh,
     *    and each mesh referencing one material
     *    In either case, for rendering, you can simply render all meshes in order - you don't need to pay attention to local transformations and the node hierarchy.
     *    Animations are removed during this step
     *    This step is intended for applications without a scenegraph.
     *    The step CAN cause some problems: if e.g. a mesh of the asset contains normals and another, using the same material index,
     *    does not, they will be brought together, but the first mesh's part of the normal list is zeroed. However, these artifacts are rare.
     *   </li>
     * </ul>
     *
     * @param modelId   The ID of the model.
     * @param modelPath Path to the model file.
     * @return Model object.
     */
    fun loadModel(modelId: String, modelPath: String): Model {
        LOGGER.trace("Try loading file {} from {}", modelId, modelPath)

        val aiScene = loadAIScene(modelId, modelPath)


        val file = File(modelPath)
        val modelDir = file.parent


        val numMaterials = aiScene.mNumMaterials()
        val materialList: MutableList<Material> = ArrayList()
        for (ic in 0 until numMaterials) {
            val aiMaterial = AIMaterial.create(aiScene.mMaterials()?.get(ic) ?: continue)
            LOGGER.trace("Try processing material {}", ic)
            materialList.add(processMaterial(aiMaterial, modelDir))
        }

        val numMeshes = aiScene.mNumMeshes()
        val aiMeshes = aiScene.mMeshes()
        val defaultMaterial = Material()
        for (ic in 0 until numMeshes) {
            LOGGER.trace("Try create AI Mesh {}", ic)
            val aiMesh = AIMesh.create(aiMeshes?.get(ic) ?: continue)
            val mesh = processMesh(aiMesh)
            LOGGER.trace("Try get Material idx")
            val materialIdx = aiMesh.mMaterialIndex()
            val material = if (materialIdx >= 0 && materialIdx < materialList.size) {
                materialList[materialIdx]
            } else {
                defaultMaterial
            }
            LOGGER.trace("Try add Material to Mesh")
            material.addMesh(mesh)
        }

        if (defaultMaterial.myMeshes.isNotEmpty()) {
            LOGGER.trace("Try add default Material")
            materialList.add(defaultMaterial)
        }

        LOGGER.trace("Try instantiate Model")
        aiReleaseImport(aiScene)

        val `🎲` = Model(modelId, materialList)
        LOGGER.trace("Try return Model")
        return `🎲`
    }

    private fun processIndices(aiMesh: AIMesh): IntArray {
        LOGGER.trace("Start processing indices")
        val indices: MutableList<Int> = ArrayList()
        val numFaces = aiMesh.mNumFaces()
        val aiFaces = aiMesh.mFaces()
        for (ic in 0 until numFaces) {
            val aiFace = aiFaces[ic]
            val buffer: IntBuffer = aiFace.mIndices()
            while (buffer.remaining() > 0) {
                indices.add(buffer.get())
            }
        }
        LOGGER.trace("End processing indices")
        return indices.toIntArray()
    }

    private fun processMaterial(aiMaterial: AIMaterial, modelDir: String): Material {
        LOGGER.trace("Start processing material")
        val material = Material()
        MemoryStack.stackPush().use { stack ->
            val color = AIColor4D.create()

            //** Diffuse color if no texture is present
            val result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color)
            if (result == aiReturn_SUCCESS) {
                material.myDiffuseColor = Vector4f(color.r(), color.g(), color.b(), color.a())
            }

            //** Try load texture
            val aiTexturePath = AIString.calloc(stack)
            aiGetMaterialTexture(
                aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, null as IntBuffer?, null, null, null, null, null
            )
            val texturePath = aiTexturePath.dataString()
            //TODO: Warning
            if (texturePath.isNotEmpty()) {
                material.myTexture = Texture(File(modelDir + File.separator + File(texturePath).toPath()))
                material.myDiffuseColor = Material.DEFAULT_COLOR
            }

            return material
        }
    }

    private fun processMesh(aiMesh: AIMesh): Mesh {
        LOGGER.trace("Start processing mesh")
        val vertices = processVertices(aiMesh)
        val normals = processNormals(aiMesh)
        var textCoords = processTextCoords(aiMesh)
        val indices = processIndices(aiMesh)

        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textCoords.isEmpty()) {
            val numElements = vertices.size / 3 * 2
            textCoords = FloatArray(numElements)
        }
        LOGGER.trace("End processing mesh")
        return Mesh(vertices, normals, textCoords, indices)
    }

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

    /**
     * Loads a AIScene from a file. Generates all the standard flags for Assimp. For more details see <a href="https://javadoc.lwjgl.org/org/lwjgl/assimp/Assimp.html">Assimp</a>.
     */
    fun loadAIScene(modelId: String, modelPath: String): AIScene {
        LOGGER.trace("Try loading file {} from {}", modelId, modelPath)

        val file = File(modelPath)
        if (!file.exists()) {
            throw RuntimeException("Model path does not exist [$modelPath]")
        }
        LOGGER.trace("Loading aiScene")

        return aiImportFile(
            modelPath,
            aiProcess_JoinIdenticalVertices or aiProcess_GlobalScale or aiProcess_FixInfacingNormals or aiProcess_Triangulate or aiProcess_CalcTangentSpace or aiProcess_LimitBoneWeights or aiProcess_PreTransformVertices
        ) ?: throw RuntimeException("Error loading model [modelPath: $modelPath]")
    }


    /**
     * Export the model to a file. (gltf or obj)
     *
     * @param model The model to export.
     * @param format The format in which the file will be saved (e.g., "gltf" or "obj").
     * @param outputPath The path where the model will be saved.
     */
    fun export(aiScene: AIScene, format: String, outputPath: String) {
        if ((format.lowercase(Locale.getDefault()) != "obj") and (format.lowercase(Locale.getDefault()) != "gltf")) {
            throw RuntimeException("Error exporting scene to $outputPath. Format not supported: $format")
        }

        val exportFlags = aiProcess_Triangulate or aiProcess_FlipUVs

        println("Exporting scene to $outputPath")

        if (format.lowercase(Locale.getDefault()) == "obj") {
            // Export the scene to OBJ format
            val result = aiExportScene(aiScene, format, outputPath, exportFlags)

            if (result == aiReturn_SUCCESS) {
                println("Scene exported successfully to $outputPath")
            } else {
                println("Error exporting scene to $outputPath. Result code: $result")
            }
        } else if (format.lowercase(Locale.getDefault()) == "gltf") {
            // Export the scene to glTF format
            // Set additional export flags for glTF 2.0
            val gltfExportFlags = exportFlags or aiProcessPreset_TargetRealtime_MaxQuality

            val result = aiExportScene(aiScene, "gltf2", outputPath, gltfExportFlags)

            if (result == aiReturn_SUCCESS) {
                println("Scene exported successfully to $outputPath")
            } else {
                println("Error exporting scene to $outputPath. Result code: $result")
            }
        }
    }


}