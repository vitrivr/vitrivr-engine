package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.types.Value
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enumeration of [Distance] functions supported by [ProximityQuery].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
enum class Distance {
    MANHATTAN {
        override fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float {
            var sum = 0.0f
            for (i in v1.value.indices) {
                sum += abs(v1.value[i] - v2.value[i])
            }
            return sum
        }

        override fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double {
            var sum = 0.0
            for (i in v1.value.indices) {
                sum += abs(v1.value[i] - v2.value[i])
            }
            return sum
        }
    },
    EUCLIDEAN {
        override fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float {
            var sum = 0.0f
            for (i in v1.value.indices) {
                sum += (v1.value[i] - v2.value[i]).pow(2)
            }
            return sqrt(sum)
        }

        override fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double {
            var sum = 0.0
            for (i in v1.value.indices) {
                sum += (v1.value[i] - v2.value[i]).pow(2)
            }
            return sqrt(sum)
        }
    },
    COSINE {
        override fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float {
            var dotProduct = 0.0f
            var normV1 = 0.0f
            var normV2 = 0.0f
            for (i in v1.value.indices) {
                dotProduct += v1.value[i] * v2.value[i]
                normV1 += v1.value[i].pow(2)
                normV2 += v2.value[i].pow(2)
            }
            normV1 = sqrt(normV1)
            normV2 = sqrt(normV2)
            return 1f - (dotProduct / (normV1 * normV2))
        }

        override fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double {
            var dotProduct = 0.0
            var normV1 = 0.0
            var normV2 = 0.0
            for (i in v1.value.indices) {
                dotProduct += v1.value[i] * v2.value[i]
                normV1 += v1.value[i].pow(2)
                normV2 += v2.value[i].pow(2)
            }
            normV1 = sqrt(normV1)
            normV2 = sqrt(normV2)
            return 1.0 - (dotProduct / (normV1 * normV2))
        }
    },
    INNER {
        override fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float {
            var innerProduct = 0.0f
            for (i in v1.value.indices) {
                innerProduct += v1.value[i] * v2.value[i]
            }
            return -1.0f * innerProduct
        }

        override fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double {
            var innerProduct = 0.0
            for (i in v1.value.indices) {
                innerProduct += v1.value[i] * v2.value[i]
            }
            return -1.0f * innerProduct
        }
    },
    HAMMING {
        override fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float {
            var sum = 0.0f
            for (i in v1.value.indices) {
                sum += if (v1.value[i] != v2.value[i]) 1.0f else 0.0f
            }
            return sum
        }

        override fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double {
            var sum = 0.0
            for (i in v1.value.indices) {
                sum += if (v1.value[i] != v2.value[i]) 1.0 else 0.0
            }
            return sum
        }
    },
    JACCARD {
        override fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float = throw UnsupportedOperationException("Jaccard distance is not supported for float vectors.")
        override fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double = throw UnsupportedOperationException("Jaccard distance is not supported for float vectors.")
    };


    /**
     * Calculates this [Distance] between two [Value.FloatVector].
     *
     * @param v1 [Value.FloatVector] First vector for distance calculation.
     * @param v2 [Value.FloatVector] Second vector for distance calculation.
     * @return [Float]
     */
    abstract operator fun invoke(v1: Value.FloatVector, v2: Value.FloatVector): Float

    /**
     * Calculates this [Distance] between two [Value.DoubleVector].
     *
     * @param v1 [Value.DoubleVector] First vector for distance calculation.
     * @param v2 [Value.DoubleVector] Second vector for distance calculation.
     * @return [Double]
     */
    abstract operator fun invoke(v1: Value.DoubleVector, v2: Value.DoubleVector): Double
}

