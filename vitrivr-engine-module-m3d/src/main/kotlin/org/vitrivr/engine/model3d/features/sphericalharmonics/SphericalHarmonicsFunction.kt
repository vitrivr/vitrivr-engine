package org.vitrivr.engine.model3d.features.sphericalharmonics

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.util.CombinatoricsUtils
import org.apache.commons.math3.util.FastMath
import kotlin.math.abs
import kotlin.math.cos

class SphericalHarmonicsFunction(l: Int, m: Int) {
    /** Degree of the Spherical Harmonics function (l >= 0) */
    private val l: Int

    /** Order of the Spherical Harmonics function (-l >= m >= l) */
    private val m: Int

    /** Normalization factor for spherical harmonic function. */
    private val Nlm: Double

    /** [AssociatedLegendrePolynomial] used to calculate the Spherical Harmonic function. */
    private val legendre: AssociatedLegendrePolynomial

    init {
        require(l >= 0) { "Spherical harmonics functions are not defined for l < 0." }
        require(!(abs(m.toDouble()) > l)) { "Spherical harmonics functions are not defined for |m| > l." }

        this.l = l
        this.m = m

        /* Calculate constants. */
        this.Nlm = getFactor(l, m)

        /* Instantiate associated legendre polynomial. */
        this.legendre = AssociatedLegendrePolynomial(l, abs(m.toDouble()).toInt())
    }

    /**
     * Compute the value of the function.
     *
     * @param theta Point at which the function value should be computed.
     * @param phi   Point at which the function value should be computed.
     * @return the complex value of the function.
     */
    fun value(theta: Double, phi: Double): Complex {
        val r: Double = this.Nlm * legendre.value(cos(theta))
        val arg = this.m * phi
        return Complex(r * FastMath.cos(arg), r * Math.sin(arg))
    }

    companion object {
        /**
         * Calculates and returns the normalisation factor for the Spherical Harmonics Function
         *
         * @param l Order
         */
        fun getFactor(l: Int, m: Int): Double {
            return FastMath.sqrt(
                ((2 * l + 1) / (4 * Math.PI)) * (CombinatoricsUtils.factorial(l - FastMath.abs(m)) / CombinatoricsUtils.factorial(
                    l + FastMath.abs(m)
                ))
            )
        }

        /**
         * Calculates and returns the number of coefficients that are expected for all spherical harmonics up to max_l.
         *
         * @param maxL The maximum harmonic to consider.
         */
        fun numberOfCoefficients(maxL: Int, onesided: Boolean): Int {
            var number = 0
            for (l in 0..maxL) {
                if (onesided) {
                    for (m in 0..l) {
                        number += 1
                    }
                } else {
                    for (m in -l..l) {
                        number += 1
                    }
                }
            }
            return number
        }
    }
}