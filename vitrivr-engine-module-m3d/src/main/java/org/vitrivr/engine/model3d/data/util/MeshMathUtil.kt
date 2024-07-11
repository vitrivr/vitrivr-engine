package org.vitrivr.engine.model3d.data.util

import org.joml.Vector3f
import org.joml.Vector3fc
import org.vitrivr.engine.core.model.mesh.Mesh
import kotlin.math.sqrt

/**
 * A collection of utilities surrounding Mesh mathematics (see [1]).  Includes methods to calculate the barycenter
 * or the bounding box of a Mesh.
 *
 * [1] Vranić, D. and D. S. (n.d.). 3D Model Retrieval.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object MeshMathUtil {
    /** Definition of the golden ratio PHI. */
    val PHI: Double = ((1.0 + sqrt(5.0)) / 2.0)

    /** Square-root of three. */
    val SQRT3: Double = sqrt(3.0)

    /** Square-root of two. */
    val SQRT2: Double = sqrt(2.0)

    /** Square-root of two. */
    val SQRT1_5: Double = sqrt(1.5)

    /**
     * Returns the vertex from a mesh that is farthest away from a given point.
     *
     * @param mesh  Mesh from which the farthest vertex should be selected.
     * @param point Point to which the distance should be calculated.
     * @return Coordinates of the vertex that is farthest to the provided point.
     */
    fun farthestVertex(mesh: Mesh, point: Vector3fc): Mesh.Vertex {
        var max: Mesh.Vertex = mesh.getVertex(0)
        var dsqMax = point.distanceSquared(max.position)
        for (v in mesh.vertices()) {
            val dsq = point.distanceSquared(v.position)
            if (dsq > dsqMax) {
                dsqMax = dsq
                max = v
            }
        }
        return max
    }

    /**
     * Returns the vertex from a mesh that is closest to a given point.
     *
     * @param mesh  Mesh from which the closest vertex should be selected.
     * @param point Point to which the distance should be calculated.
     * @return Coordinates of the vertex that is closest to the provided point.
     */
    fun closestVertex(mesh: Mesh, point: Vector3fc): Mesh.Vertex {
        var min: Mesh.Vertex = mesh.getVertex(0)
        var dsqMin = point.distanceSquared(min.position)
        for (v in mesh.vertices()) {
            val dsq = point.distanceSquared(v.position)
            if (dsq < dsqMin) {
                dsqMin = dsq
                min = v
            }
        }
        return min
    }

    /**
     * Calculates the center of mass (barycenter) of a polyhedral mesh by obtaining the mean of the Mesh's face centroids weighted by the area of the respective face as described in [1].
     *
     * @param mesh The mesh for which the barycenter should be calculated.
     * @return Coordinates of the barycenter.
     */
    fun barycenter(mesh: Mesh): Vector3f {
        val barycenter = Vector3f(0f, 0f, 0f)
        var total = 0.0
        for (face in mesh.faces()) {
            val area: Double = face.area
            if (area > 0.0) {
                barycenter.add(face.centroid.mul(area.toFloat()))
                total += area
            }
        }
        barycenter.div(total.toFloat())
        return barycenter
    }


    /**
     * Calculates and returns the bounds for the provided mesh.
     *
     * @param mesh Mesh for which bounds should be calculated.
     * @return Float-array spanning the bounds: {max_x, min_x, max_y, min_y, max_z, min_z}
     */
    fun bounds(mesh: org.vitrivr.engine.core.model.mesh.texturemodel.Mesh): FloatArray {
        /* Extract all vertices that are part of a face. */
        val vertices: MutableList<Vector3fc> = ArrayList(mesh.getNumVertices())
        for (face in mesh.getNormals()) {
            for (i in face as List<Vector3f>) {
                vertices.add(Vector3f(i.x, i.y, i.z))
            }
        }
        return bounds(mesh.getNormals())
    }

    /**
     * Calculates and returns the bounds for the provided mesh.
     *
     * @param vertices Vertices for which bounds should be calculated.
     * @return Float-array spanning the bounds: {max_x, min_x, max_y, min_y, max_z, min_z}
     */
    fun bounds(vertices: List<Vector3fc>): FloatArray {
        /* If no vertices are in the list, the box is zero. */
        if (vertices.isEmpty()) {
            return FloatArray(6)
        }

        /* Initialize the bounding-box. */
        val bounds = floatArrayOf(
            -Float.MAX_VALUE, Float.MAX_VALUE,
            -Float.MAX_VALUE, Float.MAX_VALUE,
            -Float.MAX_VALUE, Float.MAX_VALUE
        )

        /* Find max and min y-values. */
        for (vertex in vertices) {
            if (vertex.x() > bounds[0]) {
                bounds[0] = vertex.x()
            }
            if (vertex.x() < bounds[1]) {
                bounds[1] = vertex.x()
            }
            if (vertex.y() > bounds[2]) {
                bounds[2] = vertex.y()
            }
            if (vertex.y() < bounds[3]) {
                bounds[3] = vertex.y()
            }
            if (vertex.z() > bounds[4]) {
                bounds[4] = vertex.z()
            }
            if (vertex.z() < bounds[5]) {
                bounds[5] = vertex.z()
            }
        }

        /* Return bounding-box. */
        return bounds
    }
}
