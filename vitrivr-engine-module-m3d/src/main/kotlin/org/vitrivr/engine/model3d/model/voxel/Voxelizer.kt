package org.vitrivr.engine.model3d.model.voxel

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import org.vitrivr.engine.core.model.mesh.Mesh
import org.vitrivr.engine.model3d.util.MeshMathUtil
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.time.measureTime

/**
 * This class can be used to transform a 3D polygon [Mesh] into a 3D [VoxelModel].
 *
 * The class performs this transformation by applying the algorithm described in [1]. It can either use a grid of fixed
 * size or one that dynamically fits the bounding box of the Mesh given the resolution. The resulting [VoxelModel] approximates
 * the surface of the [Mesh]. The volumetric interior of the [VoxelModel] is not filled!
 *
 * <strong>Important:</strong> The resolution of the Voxelizer and the size of the Mesh determine the accuracy of the
 * voxelization process. For instance, if the resolution is chosen such that one Voxel has the size of the whole mesh,
 * the voxelization will not yield any meaningful result.
 * <p>
 *
 * [1] Huang, J. H. J., Yagel, R. Y. R., Filippov, V. F. V., & Kurzion, Y. K. Y. (1998).
 *  An accurate method for voxelizing polygon meshes. IEEE Symposium on Volume Visualization (Cat. No.989EX300), 119–126,.
 *  http://doi.org/10.1109/SVV.1998.729593
 *
 *  @author Ralph Gasser
 *  @version 1.0.0
 */
class Voxelizer(private val resolution: Float) {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /** Half of the resolution. Pre-calculated for convenience. */
    private val rc = this.resolution / 2

    /** Half the resolution squared. Pre-calculated for convenience. */
    private val rcsq = this.rc.pow(2.0f)

    /**
     * Voxelizes the provided mesh returning a VoxelGrid with the specified resolution. The size of the VoxelGrid will be chose so as to fit the size of the mesh.
     *
     * @param mesh Mesh that should be voxelized.
     * @return VoxelGrid representation of the mesh.
     */
    fun voxelize(mesh: Mesh): VoxelModel {
        /* Calculate bounding box of mesh. */
        val boundingBox: FloatArray = MeshMathUtil.bounds(mesh)
        val sizeX = (abs(ceil(((boundingBox[0] - boundingBox[1]) / this.resolution).toDouble())) + 1).toInt().toShort()
        val sizeY = (abs(ceil(((boundingBox[2] - boundingBox[3]) / this.resolution).toDouble())) + 1).toInt().toShort()
        val sizeZ = (abs(ceil(((boundingBox[4] - boundingBox[5]) / this.resolution).toDouble())) + 1).toInt().toShort()

        /* Return the voxelized mesh. */
        return this.voxelize(mesh, sizeX.toInt(), sizeY.toInt(), sizeZ.toInt())
    }

    /**
     * Voxelizes the provided mesh returning a new VoxelGrid with the specified resolution. The size of the VoxelGrid will be fixed
     *
     * @param mesh  Mesh that should be voxelized.
     * @param sizeX Number of Voxels in X direction.
     * @param sizeY Number of Voxels in Y direction.
     * @param sizeZ Number of Voxels in Z direction.
     * @return VoxelGrid representation of the mesh.
     */
    fun voxelize(mesh: Mesh, sizeX: Int, sizeY: Int, sizeZ: Int): VoxelModel {
        /* Initializes a new voxel-grid. */
        val grid = VoxelModel(sizeX, sizeY, sizeZ, this.resolution)

        /* Return the voxelized mesh. */
        return this.voxelize(mesh, grid)
    }

    /**
     * Voxelizes the provided [Mesh] into the provided [VoxelModel]
     *
     * @param mesh [Mesh] that should be voxelized.
     * @param grid [VoxelModel] to use for voxelization.
     * @return [VoxelModel] representation of the mesh.
     */
    fun voxelize(mesh: Mesh, grid: VoxelModel): VoxelModel {
        /* Process the faces and perform all the relevant tests described in [1]. */
        val duration = measureTime {
            for (face in mesh.faces()) {
                val vertices: List<Mesh.Vertex> = face.vertices
                val enclosings = this@Voxelizer.enclosingGrid(vertices, grid)
                for (enclosing in enclosings) {
                    /* Perform vertex-tests. */
                    if (this@Voxelizer.vertextTest(vertices[0], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                        continue
                    }
                    if (this@Voxelizer.vertextTest(vertices[1], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                        continue
                    }
                    if (this@Voxelizer.vertextTest(vertices[2], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                        continue
                    }
                    /* Perform edge-tests. */
                    if (this@Voxelizer.edgeTest(vertices[0], vertices[1], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                        continue
                    }
                    if (this@Voxelizer.edgeTest(vertices[1], vertices[2], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                        continue
                    }
                    if (this@Voxelizer.edgeTest(vertices[2], vertices[0], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                        continue
                    }

                    /* Perform plane-tests. */
                    if (this@Voxelizer.planeTest(vertices[0], vertices[1], vertices[2], enclosing)) {
                        grid[enclosing.first.x, enclosing.first.y, enclosing.first.z] = true
                    }
                }
            }
        }

        LOGGER.debug("Voxelization of mesh completed in {} ms (Size: {} x {} x {}).", duration, grid.sizeX, grid.sizeY, grid.sizeZ)
        return grid
    }

    /**
     * Performs the vertex-test described in [1]. Checks if the provided voxel's center is within the area of circle with radius L/2 around the vertex (L being the size of a voxel).
     *
     * @param vertex Vertex to be tested.
     * @param voxel  Voxel to be tested.
     * @return true if the voxel's center is within the circle, false otherwise.
     */
    private fun vertextTest(vertex: Mesh.Vertex, voxel: Pair<Vector3i, Vector3f>): Boolean {
        return vertex.position.distanceSquared(voxel.second) > this.rcsq
    }

    /**
     * Performs the edge-test described in [1]. Checks if the provided voxel's center is enclosed in the cylinder around the line between vertex a and vertex b.
     *
     * @param a     First vertex that constitutes the line used to draw a cylinder around.
     * @param b     Second vertex that constitutes the line used to draw a cylinder around.
     * @param voxel Voxel to be tested.
     * @return true if voxel's center is contained in cylinder, false otherwise.
     */
    private fun edgeTest(a: Mesh.Vertex, b: Mesh.Vertex, voxel: Pair<Vector3i, Vector3f>): Boolean {
        val line: Vector3f = Vector3f(b.position).sub(a.position)
        val pd = voxel.second.sub(a.position)

        /* Calculate distance between a and b (Edge). */
        val lsq: Float = a.position.distanceSquared(b.position)
        val dot = line.dot(pd)

        if (dot < 0.0f || dot > lsq) {
            return false
        } else {
            val dsq = pd.lengthSquared() - (dot.pow(2.0f)) / lsq
            return dsq > rc
        }
    }

    /**
     * Performs a simplified version of the plane-test described in [1]. Checks if the provided voxel's center is enclosed in the space spanned by two planes parallel to the face.
     *
     *
     * The original version of the test performs three addition checks with planes that go through the edges. These tests are ommited because we only work on a reduced set of voxels that directly enclose the vertices in question.
     *
     * @param a     First vertex that spans the face.
     * @param b     Second vertex that spans the face.
     * @param c     Third vertex that spans the face.
     * @param voxel Voxel to be tested.
     * @return true if voxel's center is contained in the area, false otherwise.
     */
    private fun planeTest(a: Mesh.Vertex, b: Mesh.Vertex, c: Mesh.Vertex, voxel: Pair<Vector3i, Vector3f>): Boolean {
        /* Retrieve center and corner of voxel. */
        val vcenter = voxel.second
        val vcorner = Vector3f(this.rc, this.rc, this.rc).add(vcenter)

        /* Calculate the vectors spanning the plane of the facepolyon and its plane-normal. */
        val ab: Vector3f = Vector3f(b.position).sub(a.position)
        val ac: Vector3f = Vector3f(c.position).sub(a.position)
        val planenorm = Vector3f(ab).cross(ac)

        /* Calculate the distance t for enclosing planes. */
        val t = (this.rc * MeshMathUtil.SQRT3 * vcorner.angleCos(vcenter)).toFloat()

        /* Derive new displaced plane normals. */
        val planenorm_plus = Vector3f(planenorm.x + t, planenorm.y + t, planenorm.z + t)
        val planenorm_minus = Vector3f(planenorm.x - t, planenorm.y - t, planenorm.z - t)

        /* Check if the center is under the planenorm_plus and above the planenorm_minus. */
        return planenorm_plus.dot(vcenter) < 0 && planenorm_minus.dot(vcenter) > 0
    }

    /**
     * Calculates and returns the enclosing grid, i.e. a list of Voxels from the grid that enclose the list of provided vertices.
     *
     * @param vertices The Vertices for which an enclosing grid needs to be found.
     * @param grid     VoxelGrid to select voxels from.
     * @return List of voxels that confine the provided vertices.
     */
    private fun enclosingGrid(vertices: List<Mesh.Vertex>, grid: VoxelModel): List<Pair<Vector3i, Vector3f>> {
        /* Calculate bounding box for provided vertices. */
        val positions = ArrayList<Vector3fc>(vertices.size)
        for (vertex in vertices) {
            positions.add(vertex.position)
        }

        val bounds: FloatArray = MeshMathUtil.bounds(positions)

        /* Derive max and min voxel-indices from bounding-boxes. */
        val max: Vector3i = grid.coordinateToVoxel(Vector3f(bounds[0], bounds[2], bounds[4]))
        val min: Vector3i = grid.coordinateToVoxel(Vector3f(bounds[1], bounds[3], bounds[5]))

        /* Initialize an empty ArrayList for the Voxel-Elements. */
        val enclosing: MutableList<Pair<Vector3i, Vector3f>> =
            ArrayList((max.x - min.x) * (max.y - min.y) * (max.z - min.z))
        for (i in min.x..max.x) {
            for (j in min.y..max.y) {
                for (k in min.z..max.z) {
                    enclosing.add(Pair(Vector3i(i, j, k), grid.getVoxelCenter(i, j, k)))
                }
            }
        }

        /* Return list of enclosing voxels. */
        return enclosing
    }
}