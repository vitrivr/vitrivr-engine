package org.vitrivr.engine.core.model.mesh

import org.joml.Vector3f
import org.joml.Vector3i
import org.vitrivr.engine.core.model.mesh.Mesh.Face
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * The [Mesh] is the geometric representation of a 3D model. It contains the vertices, faces, normals, and
 * texture coordinates. It also constructs the face normals and the minimal bounding box.
 *
 * @version 1.0.0
 * @author Raphael Waltenspuel
 * @author Rahel Arnold
 * @author Ralph Gasser
 */
data class Mesh(
    var id: String? = null,

    /** Array of all vertex positions in the mesh. */
    val vertexPositions: List<Vector3f>,

    /** Array of all vertex normals in the mesh. */
    val vertexNormals: List<Vector3f>,

    /** Array of all texture coordinates in the mesh. */
    val textureCoordinates: List<Vector3f>,

    /** An [IntArray] containing all indexes of a [Face]. */
    val faceIndexes: List<Vector3i>,
) {
    companion object {

        /**
         * Constructs a [Mesh] from a bunch of [FloatArray] and [IntArray]s.
         *
         * For all arrays provided, triples of values will be interpreted as components of coordiantes belonging together.
         * For example: 0, 2, 3, 5, 2, 5 == [0, 2, 3], [5, 2, 5]
         *
         * @param vertexPositions [FloatArray] of [Vertex] positions.
         * @param vertexNormals [FloatArray] of [Vertex] normals
         * @param textureCoordinates [FloatArray] of texture coordinates.
         * @param faceIndexes [IntArray] Array of face indexes.
         */
        fun of(vertexPositions: FloatArray, vertexNormals: FloatArray?, textureCoordinates: FloatArray, faceIndexes: IntArray): Mesh {
            val positions = vertexPositions.asSequence().windowed(3, 3).map { Vector3f(it[0], it[1], it[2]) }.toList()
            val normals = vertexNormals?.asSequence()?.windowed(3, 3)?.map { Vector3f(it[0], it[1], it[2]) }?.toList()
                ?: List(positions.size) { Vector3f(0.0f, 0.0f, 0.0f) }
            val coordinates = textureCoordinates.asSequence().windowed(3, 3).map { Vector3f(it[0], it[1], it[2]) }.toList()
            val indexes = faceIndexes.asSequence().windowed(3, 3).map { Vector3i(it[0], it[1], it[2]) }.toList()
            return Mesh(vertexPositions = positions, vertexNormals = normals, textureCoordinates = coordinates, faceIndexes = indexes)
        }
    }

    init {
        /* Perform some santiy checks. */
        require(vertexPositions.size == vertexNormals.size) { "The number of vertex positions and vertex normals must be equal."}
    }

    /** Number of all [Vertex] in the [Mesh]. */
    val numberOfVertices: Int
        get() = this.vertexPositions.size

    /** Number of all [Face]s in the [Mesh]. */
    val numberOfFaces: Int
        get() = this.faceIndexes.size

    /**
     * List of all face normal [Vector3f] in the [Mesh].
     *
     * The length of the normals describes the area of the face.
     * The direction of the normals describes the direction of the face and points outwards.
     */
    val faceNormals: List<Vector3f> by lazy {
        val list = ArrayList<Vector3f>(this.numberOfVertices)
        for (ic in this.faceIndexes.indices step 3) {
            if (ic == faceIndexes.size-2) {
                // reached end of loop
                break
            }

            // Get the three vertices of the face
            val v1 = this.vertexPositions[this.faceIndexes[ic].x]
            val v2 = this.vertexPositions[this.faceIndexes[ic].y]
            val v3 = this.vertexPositions[this.faceIndexes[ic].z]

            // Get the three vertices normals of the face
            val vn1 = this.vertexNormals[this.faceIndexes[ic].x]
            val vn2 = this.vertexNormals[this.faceIndexes[ic].y]
            val vn3 = this.vertexNormals[this.faceIndexes[ic].z]

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
            list.add(fn.mul(fa.length()))
        }
        list
    }

    /** [MinimalBoundingBox] that encloses this [Mesh]. */
    val minimalBoundingBox: MinimalBoundingBox by lazy { MinimalBoundingBox(this.vertexPositions) }

    /**
     * Returns an [Iterator] for all the [Face]s contained in this [Mesh].
     *
     * @return [Iterator] of [Face]s
     */
    fun faces(): Iterator<Face> = object: Iterator<Face> {
        private var index: Int = 0
        override fun hasNext(): Boolean = this.index <= this@Mesh.faceIndexes.size
        override fun next(): Face = Face(this.index++)
    }

    /**
     *
     */
    fun getVertex(index: Int) = Vertex(index)

    /**
     * Returns an [Iterator] for all the [Vertex]s contained in this [Mesh].
     *
     * @return [Iterator] of [Vertex]s
     */
    fun vertices(): Iterator<Vertex> = object: Iterator<Vertex> {
        private var index: Int = 0
        override fun hasNext(): Boolean = this.index <= this@Mesh.numberOfVertices
        override fun next(): Vertex = Vertex(this.index++)
    }

    /**
     * A geometric [Vertex] for this [Face]
     */
    inner class Vertex(index: Int) {
        /** Position of the vertex in 3D space. */
        val position: Vector3f = this@Mesh.vertexPositions[index]

        /** Position of the vertex in 3D space. */
        val normals: Vector3f = this@Mesh.vertexNormals[index]
    }

    /**
     * A geometric face for this [Mesh].
     */
    inner class Face(index: Int) {
        /** The [Vertex] objects that make-up this [Face].  */
        val vertices: List<Vertex> = listOf(
            Vertex(this@Mesh.faceIndexes[index].x),
            Vertex(this@Mesh.faceIndexes[index].y),
            Vertex(this@Mesh.faceIndexes[index].z)
        )

        /** The [Face] normal of this [Face]. */
        val normal: Vector3f = this@Mesh.faceNormals[index]

        /** The centroid for this [Face]. */
        val centroid: Vector3f by lazy {
            val centroid = Vector3f(0f, 0f, 0f)
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
            val edges: MutableList<Vector3f> = ArrayList()
            edges.add(Vector3f(v1).sub(v2))
            edges.add(Vector3f(v2).sub(v3))
            edges.add(Vector3f(v3).sub(v1))

            edges.sortWith{ o1: Vector3f, o2: Vector3f ->
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
