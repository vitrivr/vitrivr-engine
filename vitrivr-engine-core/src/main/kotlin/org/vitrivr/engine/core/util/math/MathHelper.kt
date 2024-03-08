package org.vitrivr.engine.core.util.math

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A collection of helper functions for common, mathematical operations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object MathHelper {
    /**
     * Normalizes a [FloatArray] with respect to the L2 (euclidian) norm. The method will return a new array and leave the original array unchanged.
     *
     * @param v Array that should be normalized.
     * @return Normalized array.
     */
    fun normalizeL2(v: FloatArray): FloatArray {
        val norm: Double = MathHelper.normL2(v)
        if (norm > 0.0f) {
            val vn = FloatArray(v.size)
            for (i in v.indices) {
                vn[i] = (v[i] / norm).toFloat()
            }
            return vn
        } else {
            return v
        }
    }

    /**
     * Calculates and returns the L2 (euclidian) norm of a float array.
     *
     * @param v Float array for which to calculate the L2 norm.
     * @return L2 norm
     */
    fun normL2(v: FloatArray): Double {
        var dist = 0f
        for (i in v.indices) {
            dist += v[i].pow(2.0f)
        }
        return sqrt(dist.toDouble())
    }
}