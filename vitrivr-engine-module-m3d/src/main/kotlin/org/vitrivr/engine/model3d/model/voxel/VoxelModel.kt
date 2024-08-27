package org.vitrivr.engine.model3d.model.voxel

import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import java.util.BitSet
import kotlin.math.ceil

/**
 * This class represents a [VoxelModel], i.e., a 3-dimensional grid of 3D pixels (called Voxels).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class VoxelModel(
    /** The size of the voxel grid in X direction. */
    val sizeX: Int,

    /** The size of the voxel grid in X direction. */
    val sizeY: Int,

    /** The size of the voxel grid in X direction. */
    val sizeZ: Int,

    /** Determines the size of a single voxel. */
    val resolution: Float
) {
    /** The total length ot the voxel grid (i.e., the number of voxels in the grid). */
    val length: Int = this.sizeX * this.sizeY * this.sizeZ

    /**
     * Defines the center of the voxel-grid (in the world coordinate system). It corresponds to the center of the voxel at (sizeX/2, sizeY/2, sizeZ/2).
     *
     * Important:  Transformation into world coordinates are based on this center!
     */
    var center = Vector3f(0f, 0f, 0f)
        private set

    /**
     * Number of visible voxels in the grid.
     */
    var visible: Int = 0
        private set

    /**
     * Number of invisible voxels in the grid.
     */
    var invisible: Int = 0
        private set

    /**
     * Array holding the actual voxels.
     */
    private val voxelGrid = BitSet(this.sizeX * this.sizeY * this.sizeZ)

    val gridCenter: Vector3fc
        get() = this.center

    /**
     * Returns true if VoxelGrid is visible (i.e. there is at least one visible Voxel) and false otherwise.
     */
    fun isVisible(): Boolean {
        return this.visible > 0
    }

    /**
     * Transforms world-coordinates (e.g. position of a vertex in space) into the corresponding voxel coordinates, i.e. the index of the voxel in the grid.
     *
     *
     * Important:  The indices returned by this method are not necessarily within the bounds
     * of the grid.
     *
     * @param coordinate Coordinates to be transformed.
     * @return coordinate Voxel indices.
     */
    fun coordinateToVoxel(coordinate: Vector3fc?): Vector3i {
        val gridCoordinates = Vector3f(coordinate).add(this.center).div(this.resolution)
        return Vector3i(
            ceil((gridCoordinates.x + this.sizeX / 2).toDouble()).toInt(),
            ceil((gridCoordinates.y + this.sizeY / 2).toDouble()).toInt(),
            ceil((gridCoordinates.z + this.sizeZ / 2).toDouble()).toInt()
        )
    }

    /**
     * Returns the Voxel at the specified position.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     */
    operator fun get(x: Int, y: Int, z: Int): Boolean {
        val index = coordinatesToIndex(x, y, z)
        return this.voxelGrid[index]
    }

    /**
     * Toggles the Voxel at the specified position.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @param visible If true, the new Voxel position will become visible.
     */
    operator fun set(x: Int, y: Int, z: Int, visible: Boolean) {
        val index = coordinatesToIndex(x, y, z)
        if (visible && !this.voxelGrid[index]) {
            this.invisible -= 1
            this.visible += 1
        } else if (!visible && this.voxelGrid[index]) {
            this.invisible += 1
            this.visible -= 1
        }
    }

    /**
     * Calculates and returns the center of the Voxel in a 3D coordinate system using the grids resolution property.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @return org.vitrivr.engine.core.model.types.Vector3f containing the center of the voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    fun getVoxelCenter(x: Int, y: Int, z: Int): Vector3f {
        return Vector3f(
            (x - this.sizeX / 2) * this.resolution + this.center.x,
            (y - this.sizeY / 2) * this.resolution + this.center.y,
            (z - this.sizeZ / 2) * this.resolution + this.center.z
        )
    }

    /**
     * Converts the VoxelGrid into a string that can be read by Matlab (e.g. for 3D scatter plots). The array contains the coordinates of all visible voxels.
     *
     * @return String
     */
    fun toMatlabArray(): String {
        val buffer = StringBuilder()
        buffer.append("[")
        for (x in 0 until this.sizeX) {
            for (y in 0 until this.sizeY) {
                for (z in 0 until this.sizeZ) {
                    val index = (z * this.sizeZ * this.sizeY) + (this.sizeY * y) + x
                    if (!voxelGrid[index]) {
                        buffer.append(String.format("%d %d %d; ", x, y, z))
                    }
                }
            }
        }
        buffer.append("]")
        return buffer.toString()
    }

    /**
     * Converts 3D coordinates (x, y, z) to a linear index.
     *
     * @param x position of the Voxel.
     * @param y position of the Voxel.
     * @param z position of the Voxel.
     * @return Linear bit index.
     */
    private fun coordinatesToIndex( x: Int, y: Int, z: Int): Int {
        require(x < this.sizeX) { "X-coordinate $x is out of bounds for size ${this.sizeX}." }
        require(y < this.sizeY) { "X-coordinate $x is out of bounds for size ${this.sizeY}." }
        require(z < this.sizeZ) { "X-coordinate $x is out of bounds for size ${this.sizeZ}." }
        return (z * this.sizeZ * this.sizeY) + (this.sizeY * y) + x
    }
}