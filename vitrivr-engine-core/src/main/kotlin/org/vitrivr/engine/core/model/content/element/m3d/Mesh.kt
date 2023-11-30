package org.vitrivr.engine.core.model.content.element.m3d

import org.apache.logging.log4j.LogManager
import org.joml.Vector3f
import org.vitrivr.engine.core.model.content.element.m3d.util.MinimalBoundingBox
import java.util.*

/**
 * The Mesh is the geometric representation of a model.
 * It contains the vertices, faces, normals, and texture coordinates.
 * It also constructs the face normals and the minimal bounding box.
 */
class Mesh(
    positions: FloatArray,
    normals: FloatArray?,
    textureCoordinates: FloatArray,
    idx: IntArray
) {
    private val LOGGER = LogManager.getLogger()

    /**
     * Number of all vertices in the mesh.
     */
    private val numVertices: Int

    /**
     * ID of the mesh.
     */
    private var id: String? = null

    /**
     * List of all vertices in the mesh.
     * The positions are flattened vectors.
     * positions[0] = x
     * positions[1] = y
     * positions[2] = z
     * positions[3] = x
     * ...
     */
    private val positions: FloatArray

    /**
     * List of all face normals in the mesh.
     * The length of the normals describes the area of the face.
     * The direction of the normals describes the direction of the face and points outwards.
     */
    private val facenormals: MutableList<Vector3f>

    /**
     * List of all texture coordinates in the mesh.
     */
    private val textureCoords: FloatArray

    /**
     * Flattened list of all vertices ids.
     * A three tuple describes a face.
     * e.g.  0, 1, 3, 3, 1, 2,
     * face1 = (0, 1, 3)
     * face2 = (3, 1, 2)
     */
    private val idx: IntArray

    /**
     * List of all vertices normals in the mesh.
     * TODO: not used yet, will be used for vertex shading.
     */
    @Suppress("all")
    val normals: FloatArray?

    /**
     * MinimalBoundingBox that encloses the mesh.
     */
    private val minimalBoundingBox: MinimalBoundingBox

    /**
     * Constructor for Mesh.
     * Arrays are flattened vectors.
     * e.g. positions[0] = x
     *      positions[1] = y
     *      positions[2] = z
     *      positions[3] = x
     *      ...
     *
     * @param positions List of all vertices in the mesh.
     * @param normals List of all vertices normals in the mesh.
     * @param textureCoordinates List of all texture coordinates in the mesh.
     * @param idx List of all vertices ids.
     */
    init {
        //Stores all the data
        this.positions = positions
        this.idx = idx
        this.numVertices = idx.size
        this.normals = normals
        // List to store results of face normals calculation
        this.facenormals = ArrayList(numVertices / 3)
        this.textureCoords = textureCoordinates

        // Calculate face normals
        // ic increments by 3 because a face is defined by 3 vertices
        for (ic in idx.indices step 3) {
            if (ic == idx.size-2) {
                // reached end of loop
                break
            }
            if (normals == null) {
                // Add zero vector if there are no vertex normals
                facenormals.add(Vector3f(0f, 0f, 0f))
            } else {
                // Get the three vertices of the face
                val v1 = Vector3f(
                    positions[idx[ic] * 3],
                    positions[idx[ic] * 3 + 1],
                    positions[idx[ic] * 3 + 2]
                )
                val v2 = Vector3f(
                    positions[idx[ic + 1] * 3],
                    positions[idx[ic + 1] * 3 + 1],
                    positions[idx[ic + 1] * 3 + 2]
                )
                val v3 = Vector3f(
                    positions[idx[ic + 2] * 3],
                    positions[idx[ic + 2] * 3 + 1],
                    positions[idx[ic + 2] * 3 + 2]
                )
                // Get the three vertices normals of the face
                val vn1 = Vector3f(
                    normals[idx[ic] * 3],
                    normals[idx[ic] * 3 + 1],
                    normals[idx[ic] * 3 + 2]
                )
                val vn2 = Vector3f(
                    normals[idx[ic + 1] * 3],
                    normals[idx[ic + 1] * 3 + 1],
                    normals[idx[ic + 1] * 3 + 2]
                )
                val vn3 = Vector3f(
                    normals[idx[ic + 2] * 3],
                    normals[idx[ic + 2] * 3 + 1],
                    normals[idx[ic + 2] * 3 + 2]
                )
                // Instance the face normal
                val fn = Vector3f(0f, 0f, 0f)
                // Calculate the direction of the face normal by averaging the three vertex normals
                fn.add(vn1).add(vn2).add(vn3).div(3f).normalize()
                // Instance the face area
                val fa = Vector3f(0f, 0f, 0f)
                // Calculate the area of the face by calculating the cross product of the two edges and dividing by 2
                v2.sub(v1).cross(v3.sub(v1), fa)
                fa.div(2f)
                // Add the face normal to the list of face normals
                facenormals.add(fn.mul(fa.length()))
            }
        }
        // Calculate the minimal bounding box
        this.minimalBoundingBox = MinimalBoundingBox(positions)
    }

    /**
     * @return returns the number of vertices in the mesh.
     */
    fun getNumVertices(): Int {
        return numVertices
    }

    /**
     * @return the flattened array of all positions.
     */
    fun getPositions(): FloatArray {
        return positions
    }

    /**
     * @return the flattened array of all texture coordinates.
     */
    fun getTextureCoords(): FloatArray {
        return textureCoords
    }

    /**
     * @return the flattened array of all vertices ids.
     * A three tuple describes a face.
     * e.g  0, 1, 3, 3, 1, 2,
     * face1 = (0, 1, 3)
     * face2 = (3, 1, 2)
     */
    fun getIdx(): IntArray {
        return idx
    }

    /**
     * @return list containing all face normals.
     */
    fun getNormals(): List<Vector3f> {
        return facenormals
    }

    /**
     * @return the MinimalBoundingBox which contains the scaling factor to norm and the translation to origin (0,0,0).
     */
    fun getMinimalBoundingBox(): MinimalBoundingBox {
        return minimalBoundingBox
    }

    /**
     * @return the scaling factor to norm 1 size.
     * @deprecated use [getMinimalBoundingBox] instead.
     */
    @Deprecated("Use getMinimalBoundingBox() instead.")
    fun getNormalizedScalingFactor(): Float {
        return minimalBoundingBox.getScalingFactorToNorm()
    }

    /**
     * @return the translation to origin (0,0,0).
     * @deprecated use [getMinimalBoundingBox] instead.
     */
    @Deprecated("Use getMinimalBoundingBox() instead.")
    fun getNormalizedPosition(): Vector3f {
        return minimalBoundingBox.getTranslationToNorm()
    }

    /**
     * @return the id of the mesh.
     */
    fun getId(): String? {
        return id
    }

    /**
     * @param id sets the id of the mesh.
     */
    fun setId(id: Int) {
        this.id = id.toString()
    }

    /**
     * closes the mesh.
     * releases all resources.
     */
    fun close() {
        facenormals.clear()
        minimalBoundingBox.close()
        id = null
        LOGGER.trace("Closing Mesh")
    }
}
