package org.vitrivr.engine.core.util.math

/**
 * A collection of helper methods for basic statistics.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object StatisticsHelper {
    /**
     * Extracts the median value from a [IntArray] histogram.
     *
     * @param hist The [IntArray] representing the histogram.
     * @return The median value.
     */
    fun medianFromHistogram(hist: IntArray): Int {
        var posL = 0
        var posR = hist.size - 1
        var sumL = hist[posL]
        var sumR = hist[posR]

        while (posL < posR) {
            if (sumL < sumR) {
                sumL += hist[++posL]
            } else {
                sumR += hist[--posR]
            }
        }
        return posL
    }
}