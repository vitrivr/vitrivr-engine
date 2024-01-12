package org.vitrivr.engine.model3d.model.voxel

import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import kotlin.math.ceil

/**
 * This class represents a [Voxel] model, i.e., a 3-dimensional grid of 3D pixels (called Voxels).
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
    private val voxelGrid: Array<Array<Array<Voxel>>> = Array(this.sizeX) {
        Array(this.sizeY) {
            Array(this.sizeZ) {
                Voxel.INVISIBLE
            }
        }
    }

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
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    fun get(x: Int, y: Int, z: Int): Voxel {
        return this.voxelGrid[x][y][z]
    }

    /**
     * Returns true, if the Voxel at the specified position is visible and false otherwise.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    fun isVisible(x: Int, y: Int, z: Int): Boolean {
        return this.voxelGrid[x][y][z] == Voxel.VISIBLE
    }

    /**
     * Calculates and returns the center of the Voxel in a 3D coordinate system using the grids resolution property.
     *
     * @param x x position of the Voxel.
     * @param y y position of the Voxel.
     * @param z z position of the Voxel.
     * @return Vector3f containing the center of the voxel.
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
     * Toggles the Voxel at the specified position.
     *
     * @param visible If true, the new Voxel position will become visible.
     * @param x       x position of the Voxel.
     * @param y       y position of the Voxel.
     * @param z       z position of the Voxel.
     * @throws ArrayIndexOutOfBoundsException If one of the three indices is larger than the grid.
     */
    fun toggleVoxel(visible: Boolean, x: Int, y: Int, z: Int) {
        if (visible && this.voxelGrid[x][y][z] == Voxel.INVISIBLE) {
            this.voxelGrid[x][y][z] = Voxel.VISIBLE
            this.invisible -= 1
            this.visible += 1
        } else if (!visible && this.voxelGrid[x][y][z] == Voxel.VISIBLE) {
            this.voxelGrid[x][y][z] = Voxel.INVISIBLE
            this.invisible += 1
            this.visible -= 1
        }
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
                    if (voxelGrid[x][y][z] == Voxel.VISIBLE) {
                        buffer.append(String.format("%d %d %d; ", x, y, z))
                    }
                }
            }
        }
        buffer.append("]")
        return buffer.toString()
    }

    /**
     * Represents a single [Voxel] in a [VoxelModel] which can either can be visible or invisible.
     */
    enum class Voxel {
        VISIBLE,
        INVISIBLE
    }
}