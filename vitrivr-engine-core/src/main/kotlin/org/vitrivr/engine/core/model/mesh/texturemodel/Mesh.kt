package org.vitrivr.engine.core.model.mesh.texturemodel

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.mesh.texturemodel.util.types.Vec3f
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.vitrivr.engine.core.model.mesh.texturemodel.util.MinimalBoundingBox
import java.io.Serializable as JavaSerializable
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * The Mesh is the geometric representation of a model.
 * It contains the vertices, faces, normals, and texture coordinates.
 * It also constructs the face normals and the minimal bounding box.
 */
@Serializable
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
) : JavaSerializable {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mesh) return false

        if (!positions.contentEquals(other.positions)) return false
        if (!normals.contentEquals(other.normals)) return false
        if (!textureCoords.contentEquals(other.textureCoords)) return false
        if (!idx.contentEquals(other.idx)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = positions.contentHashCode()
        result = 31 * result + (normals?.contentHashCode() ?: 0)
        result = 31 * result + textureCoords.contentHashCode()
        result = 31 * result + idx.contentHashCode()
        return result
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
    private val facenormals: MutableList<Vec3f> = ArrayList(numVertices / 3)

    /**
     * MinimalBoundingBox that encloses the mesh.
     */
    val minBoundingBox: MinimalBoundingBox

    init {
        // Calculate face normals
        for (ic in idx.indices step 3) {
            if (normals == null) {
                // Add zero vector if there are no vertex normals
                facenormals.add(Vec3f(0f, 0f, 0f))
            } else {
                // Get the three vertices of the face
                val v1 = Vec3f(positions[idx[ic] * 3], positions[idx[ic] * 3 + 1], positions[idx[ic] * 3 + 2])
                val v2 = Vec3f(positions[idx[ic + 1] * 3], positions[idx[ic + 1] * 3 + 1], positions[idx[ic + 1] * 3 + 2])
                val v3 = Vec3f(positions[idx[ic + 2] * 3], positions[idx[ic + 2] * 3 + 1], positions[idx[ic + 2] * 3 + 2])

                // Get the three vertices normals of the face
                val vn1 = Vec3f(normals[idx[ic] * 3], normals[idx[ic] * 3 + 1], normals[idx[ic] * 3 + 2])
                val vn2 = Vec3f(normals[idx[ic + 1] * 3], normals[idx[ic + 1] * 3 + 1], normals[idx[ic + 1] * 3 + 2])
                val vn3 = Vec3f(normals[idx[ic + 2] * 3], normals[idx[ic + 2] * 3 + 1], normals[idx[ic + 2] * 3 + 2])

                // Instance the face normal
                val fn = Vec3f(0f, 0f, 0f)

                // Calculate the direction of the face normal by averaging the three vertex normals
                fn.add(vn1).add(vn2).add(vn3).div(3f).normalize()

                // Instance the face area
                val fa = Vec3f(0f, 0f, 0f)

                // Calculate the area of the face by calculating the cross product of the two edges and dividing by 2
                v2.subtract(v1).cross(v3.subtract(v1), fa)
                fa.div(2f)

                // Add the face normal to the list of face normals
                facenormals.add(fn.scale(fa.length()))
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

    fun numberOfFaces(): Int = idx.size / 3

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
    fun getNormals(): List<Vec3f> = facenormals

    /**
     * @return the MinimalBoundingBox which contains the scaling factor to norm and the translation to origin (0,0,0).
     */
    fun getMinimalBoundingBox(): MinimalBoundingBox = minBoundingBox

    fun getVertex(index: Int) = Vertex(index)


    fun faces(): Iterator<Face> = object: Iterator<Face> {
        private var index: Int = 0
        override fun hasNext(): Boolean = this.index <= this@Mesh.facenormals.size
        override fun next(): Face = Face(this.index++)
    }

    fun vertices(): Iterator<Vertex> = object: Iterator<Vertex> {
        private var index: Int = 0
        override fun hasNext(): Boolean = this.index <= this@Mesh.normals!!.size
        override fun next(): Vertex = Vertex(this.index++)
    }

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



    /**
     * A geometric [Vertex] for this [Face]
     */
    inner class Vertex(index: Int) {
        /** Position of the vertex in 3D space. */
        val position: Vec3f = Vec3f((this@Mesh.positions.get(index)) *3,this@Mesh.positions[index]*3+1, this@Mesh.positions[index]*3+2)

        /** Position of the vertex in 3D space. */
        val normals: Vec3f = Vec3f((this@Mesh.normals?.get(index)!!) *3,this@Mesh.normals[index]*3+1, this@Mesh.normals[index]*3+2)
    }

    /**
     * A geometric face for this [Mesh].
     */
    inner class Face(index: Int) {
        /** The [Vertex] objects that make-up this [Face].  */
        val vertices: List<Vertex> = listOf(
            Vertex(this@Mesh.facenormals[index].x.toInt()),
            Vertex(this@Mesh.facenormals[index].y.toInt()),
            Vertex(this@Mesh.facenormals[index].z.toInt())
        )

        /** The [Face] normal of this [Face]. */
        val normal: Vec3f = this@Mesh.facenormals[index]

        /** The centroid for this [Face]. */
        val centroid: Vec3f by lazy {
            val centroid = Vec3f(0f, 0f, 0f)
            for (vertex in this.vertices) {
                centroid.add(vertex.position)
            }
            centroid.div(3.0f)
            centroid
        }

        /** The area of this [Face]*/
        val area: Double by lazy {
            /* Extract vertices. */
            val v1 = this.vertices[0].position
            val v2 = this.vertices[1].position
            val v3 = this.vertices[2].position

            /* Generate the edges and sort them in ascending order. */
            val edges: MutableList<Vec3f> = ArrayList()
            edges.add(Vec3f(v1).subtract(v2))
            edges.add(Vec3f(v2).subtract(v3))
            edges.add(Vec3f(v3).subtract(v1))

            edges.sortWith{ o1: Vec3f, o2: Vec3f ->
                val difference = o1.length() - o2.length()
                difference.sign.toInt()
            }

            val a = edges[2].length()
            val b = edges[1].length()
            val c = edges[0].length()

            /* Returns the area of the triangle according to Heron's Formula. */
            val area: Double = 0.25 * sqrt((a + (b + c)) * (c - (a - b)) * (c + (a - b)) * (a + (b - c)))
            if (area.isNaN()) { 0.0 } else { area }
        }
    }

}