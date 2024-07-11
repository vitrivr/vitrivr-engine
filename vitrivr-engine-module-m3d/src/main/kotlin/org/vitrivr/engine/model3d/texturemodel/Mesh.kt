package org.vitrivr.engine.model3d.texturemodel

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.util.MinimalBoundingBox

/**
 * The Mesh is the geometric representation of a model.
 * It contains the vertices, faces, normals, and texture coordinates.
 * It also constructs the face normals and the minimal bounding box.
 */
class Mesh(
    /**
     * List of all vertices in the mesh.
     * The positions are flattened vectors:
     * positions[0] = x
     * positions[1] = y
     * positions[2] = z
     * positions[3] = x
     * ...
     */
    private val positions: FloatArray,

    /**
     * List of all vertices normals in the mesh.
     */
    val normals: FloatArray?,

    /**
     * List of all texture coordinates in the mesh.
     */
    private val textureCoords: FloatArray,

    /**
     * Flattered list of all vertices ids.
     * A three-tuple describes a face:
     * e.g.  0, 1, 3, 3, 1, 2,
     * face1 = (0, 1, 3)
     * face2 = (3, 1, 2)
     */
    private val idx: IntArray
) {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Number of all vertices in the mesh.
     */
    private val numVertices: Int = idx.size

    /**
     * ID of the mesh.
     */
    var id: String? = null

    /**
     * List of all face normals in the mesh.
     * The length of the normals describes the area of the face.
     * The direction of the normals describes the direction of the face and points outwards.
     */
    private val facenormals: MutableList<Vector3f> = ArrayList(numVertices / 3)

    /**
     * MinimalBoundingBox that encloses the mesh.
     */
    public val minBoundingBox: MinimalBoundingBox

    init {
        // Calculate face normals
        for (ic in idx.indices step 3) {
            if (normals == null) {
                // Add zero vector if there are no vertex normals
                facenormals.add(Vector3f(0f, 0f, 0f))
            } else {
                // Get the three vertices of the face
                val v1 = Vector3f(positions[idx[ic] * 3], positions[idx[ic] * 3 + 1], positions[idx[ic] * 3 + 2])
                val v2 = Vector3f(positions[idx[ic + 1] * 3], positions[idx[ic + 1] * 3 + 1], positions[idx[ic + 1] * 3 + 2])
                val v3 = Vector3f(positions[idx[ic + 2] * 3], positions[idx[ic + 2] * 3 + 1], positions[idx[ic + 2] * 3 + 2])

                // Get the three vertices normals of the face
                val vn1 = Vector3f(normals[idx[ic] * 3], normals[idx[ic] * 3 + 1], normals[idx[ic] * 3 + 2])
                val vn2 = Vector3f(normals[idx[ic + 1] * 3], normals[idx[ic + 1] * 3 + 1], normals[idx[ic + 1] * 3 + 2])
                val vn3 = Vector3f(normals[idx[ic + 2] * 3], normals[idx[ic + 2] * 3 + 1], normals[idx[ic + 2] * 3 + 2])

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
        minBoundingBox = MinimalBoundingBox(positions)
    }

    /**
     * @return the number of vertices in the mesh.
     */
    fun getNumVertices(): Int = numVertices

    /**
     * @return the flattened array of all positions.
     */
    fun getPositions(): FloatArray = positions

    /**
     * @return the flattened array of all texture coordinates.
     */
    fun getTextureCoords(): FloatArray = textureCoords

    /**
     * @return the flattened array of all vertices ids.
     * A three-tuple describes a face:
     * e.g.  0, 1, 3, 3, 1, 2,
     * face1 = (0, 1, 3)
     * face2 = (3, 1, 2)
     */
    fun getIdx(): IntArray = idx

    /**
     * @return list containing all face normals.
     */
    fun getNormals(): List<Vector3f> = facenormals

    /**
     * @return the MinimalBoundingBox which contains the scaling factor to norm and the translation to origin (0,0,0).
     */
    fun getMinimalBoundingBox(): MinimalBoundingBox = minBoundingBox

    /**
     * @return the scaling factor to norm 1 size.
     * @deprecated use [getMinimalBoundingBox] instead.
     */
    @Deprecated("use getMinimalBoundingBox() instead")
    fun getNormalizedScalingFactor(): Float = minBoundingBox.scalingFactorToNorm

    /**
     * @return the translation to origin (0,0,0).
     * @deprecated use [getMinimalBoundingBox] instead.
     */
    @Deprecated("use getMinimalBoundingBox() instead")
    fun getNormalizedPosition(): Vector3f = minBoundingBox.translationToNorm

    /**
     * @param id sets the id of the mesh.
     */
    fun setId(id: Int) {
        this.id = id.toString()
    }

    /**
     * Closes the mesh and releases all resources.
     */
    fun close() {
        facenormals.clear()
        minBoundingBox.close()
        id = null
        LOGGER.trace("Closing Mesh")
    }
}
