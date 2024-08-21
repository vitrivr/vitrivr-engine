package org.vitrivr.engine.core.util.math

import org.vitrivr.engine.core.model.types.Value
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A collection of helper functions for common, mathematical operations.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
object MathHelper {

    /** Square root of 2. */
    val SQRT2: Double = sqrt(2.0)

    /** Square root of 2 as a [Float]. */
    val SQRT2_f: Float = SQRT2.toFloat()

    /**
     * Normalizes a [Array] of [Value.Float] with respect to the L2 (euclidian) norm.
     * The method will return a new array and leave the original array unchanged.
     *
     * @param v Array that should be normalized.
     * @return Normalized array.
     */
    fun normalizeL2(v: Array<Value.Float>): Array<Value.Float>{
        val norm: Double = normL2(v)
        if (norm > 0.0f) {
            val vn = Array(v.size){Value.Float(0f)}
            for (i in v.indices) {
                vn[i] = Value.Float((v[i].value / norm).toFloat())
            }
            return vn
        } else {
            return v
        }
    }

    /**
     * Normalizes a [FloatArray] with respect to the L2 (euclidian) norm. The method will return a new array and leave the original array unchanged.
     *
     * @param v Array that should be normalized.
     * @return Normalized array.
     */
    fun normalizeL2(v: FloatArray): FloatArray {
        val norm: Double = normL2(v)
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

    /**
     * Calculates and returns the L2 (euclidian) norm of a float array.
     *
     * @param v Float array for which to calculate the L2 norm.
     * @return L2 norm
     */
    fun normL2(v: Array<Value.Float>): Double{
        var dist = 0f
        for (i in v.indices){
            dist += v[i].value.pow(2.0f)
        }
        return sqrt(dist.toDouble())
    }
}
