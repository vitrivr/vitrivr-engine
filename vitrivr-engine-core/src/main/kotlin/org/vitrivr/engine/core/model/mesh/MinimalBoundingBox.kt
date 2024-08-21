package org.vitrivr.engine.core.model.mesh

import org.joml.Vector3f

/**
 * This class represents a minimal bounding box. It can be generated from a list of vertices. It can be merged with another minimal bounding box.
 */
open class MinimalBoundingBox {

    /**
     * Constant for the maximum float value.
     */
    companion object {
        const val MAX: Float = Float.MAX_VALUE

        /**
         * Constant for the minimum float value.
         */
        const val MIN: Float = -1f * Float.MAX_VALUE
    }

    /**
     * Initial value for the maximum vector. The maximum vector contain the highest (positive if normalized) values for x, y and z of the bounding box.
     */
    private val vMax = Vector3f(MIN, MIN, MIN)

    /**
     * Initial value for the minimum vector. The minimum vector contain the lowest (negative if normalized) values for x, y and z of the bounding box.
     */
    private val vMin = Vector3f(MAX, MAX, MAX)

    /**
     * Center of mass of the bounding box as x, y, z vector.
     */
    private val com = Vector3f(0f, 0f, 0f)

    /**
     * Scaling factor to norm. The scaling factor is the factor to scale the bounding box to the norm. 1 for no scaling.
     */
    private var scalingFactorToNorm = 1f

    /**
     * Translation to norm. The translation is the vector to translate com of the bounding box to the origin. (0, 0, 0) for no translation.
     */
    private val translationToNorm = Vector3f(0F, 0F, 0F)

    /**
     * Empty constructor to initialize an empty bounding box The purpose is to iteratively add bounding boxes.
     */
    constructor()

    /**
     * Constructor to initialize a bounding box from an array of vertices. The resulting bounding box is the minimal bounding box that contains all vertices. The bounding box is aligned with the Cartesian coordinate system.
     *
     * @param positions List of vertices.
     */
    constructor(positions: FloatArray) {
        update(positions)
    }

    /**
     * Constructor to initialize a bounding box from a list of vertices. The resulting bounding box is the minimal bounding box that contains all vertices. The bounding box is aligned with the Cartesian coordinate system.
     *
     * @param positions List of vertices.
     */
    constructor(positions: List<Vector3f>) {
        update(positions)
    }

    /**
     * Create list of vertices from bounding box. List contains the maximum and minimum vector of the bounding box.
     * TODO: A better approach would be to return the 8 vertices of the bounding box. But needs to be checked before.
     *
     * @return List of vertices.
     */
    private fun toList(): List<Vector3f> {
        val vec = mutableListOf<Vector3f>()
        if (isValidBoundingBox()) {
            vec.add(vMax)
            vec.add(vMin)
        }
        return vec
    }

    /**
     * Merge this bounding box with another bounding box. The resulting bounding box is the minimal bounding box that contains both bounding boxes.
     *
     * @param other Bounding box to merge with.
     * @return Merged bounding box.
     */
    fun merge(other: MinimalBoundingBox): MinimalBoundingBox {
        if (this == other) {
            return this
        }
        update(other.toList())
        return this
    }

    /**
     * Returns the scaling factor to norm size.
     *
     * @return Scaling factor to norm size.
     */
    fun getScalingFactorToNorm(): Float {
        return scalingFactorToNorm
    }

    /**
     * Get translation to Origin.
     *
     * @return Translation to Origin.
     */
    fun getTranslationToNorm(): Vector3f {
        return translationToNorm
    }

    /**
     * Helper method to add data to the bounding box and recalculate the bounding boxes values.
     */
    private fun update(positions: FloatArray) {
        val vectors = positions.asList().chunked(3) { (x, y, z) -> Vector3f(x, y, z) }
        update(vectors)
    }

    /**
     * Helper method to add data to the bounding box and recalculate the bounding boxes values. Since the calculation of the bounding box is iterative, the calculation is split into several steps. The steps are:
     * <ul>
     * <li>1. Update the center of mass.</li>
     * <li>2. Update the scaling factor to norm.</li>
     * <li>3. Update the translation to norm.</li>
     * </ul>
     * These steps had to be exact in this sequence
     */
    private fun update(vec: List<Vector3f>) {
        // Has to be exact this sequence
        if (updateBounds(vec)) {
            updateCom()
            updateScalingFactorToNorm()
            updateTranslationToNorm()
        }
    }

    /**
     * Checks if the bounding box is valid. A bounding box is valid if each component of the maximum vector is greater than the corresponding component of the minimum vector.
     *
     * @return True if the bounding box is valid, false otherwise.
     */
    private fun isValidBoundingBox(): Boolean {
        return vMax.x > vMin.x && vMax.y > vMin.y && vMax.z > vMin.z
    }

    /**
     * Update the center of mass. The center of mass is the middle point of the bounding box.
     */
    private fun updateCom() {
        com.set(Vector3f((vMax.x + vMin.x) / 2f, (vMax.y + vMin.y) / 2f, (vMax.z + vMin.z) / 2f))
    }

    /**
     * Update the translation to norm. The translation is the vector to translate com of the bounding box to the origin. (0, 0, 0) for no translation.
     */
    private fun updateTranslationToNorm() {
        translationToNorm.set(com.mul(scalingFactorToNorm))
    }

    /**
     * Update the scaling factor to norm. The scaling factor is the factor to scale the longest vector in the bounding box to the norm. 1 for no scaling.
     */
    private fun updateScalingFactorToNorm() {
        var farthest = Vector3f(0F, 0F, 0F)
        for (vec in toList()) {
            val vector = Vector3f(vec).sub(com)
            if (vector.length() > farthest.length()) {
                farthest = vector
            }
            scalingFactorToNorm = 1f / (farthest.length() * 2)
        }
    }

    /**
     * Update the bounding box with new vectors
     *
     * @return True if the bounding box has changed, false otherwise.
     */
    private fun updateBounds(positions: List<Vector3f>): Boolean {
        var changed = false
        for (vec in positions) {
            changed = updateBounds(vec) || changed
        }
        return changed
    }

    /**
     * Update the bounding box with a new vector
     *
     * @return True if the bounding box has changed, false otherwise.
     */
    private fun updateBounds(vec: Vector3f): Boolean {
        var changed = false
        if (vec.x > vMax.x) {
            vMax.x = vec.x
            changed = true
        }
        if (vec.x < vMin.x) {
            vMin.x = vec.x
            changed = true
        }
        if (vec.y > vMax.y) {
            vMax.y = vec.y
            changed = true
        }
        if (vec.y < vMin.y) {
            vMin.y = vec.y
            changed = true
        }
        if (vec.z > vMax.z) {
            vMax.z = vec.z
            changed = true
        }
        if (vec.z < vMin.z) {
            vMin.z = vec.z
            changed = true
        }
        return changed
    }
}
