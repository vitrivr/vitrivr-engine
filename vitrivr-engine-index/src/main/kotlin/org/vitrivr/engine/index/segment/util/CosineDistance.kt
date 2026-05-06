package org.vitrivr.engine.index.segment.util

import kotlin.math.sqrt

fun cosineDistance(a: DoubleArray, b: DoubleArray): Double {
    val n = minOf(a.size, b.size)
    if (n == 0) return 0.0

    var dot = 0.0
    var normA = 0.0
    var normB = 0.0

    for (i in 0 until n) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }

    if (normA == 0.0 || normB == 0.0) return 0.0
    return 1.0 - (dot / (sqrt(normA) * sqrt(normB))).coerceIn(-1.0, 1.0)
}
