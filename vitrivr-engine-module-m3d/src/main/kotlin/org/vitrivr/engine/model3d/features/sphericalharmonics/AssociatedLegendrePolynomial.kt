package org.vitrivr.engine.model3d.features.sphericalharmonics

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.analysis.polynomials.PolynomialsUtils
import org.apache.commons.math3.util.FastMath
import kotlin.math.pow

class AssociatedLegendrePolynomial(l: Int, m: Int) : UnivariateFunction {
    /** Legendre used to generate base values for the associated Legendre polynomial. That polynomial is the m-th derivative of the l-th Legendre polynomial. */
    private val legendre: PolynomialFunction

    /** Degree of the [AssociatedLegendrePolynomial]. */
    val l: Int

    /** Order of the [AssociatedLegendrePolynomial]. */
    val m: Int

    /**
     * Sign of the polynomial which is only determined by m.
     */
    private val sign: Double

    /**
     * Constructor for the AssociatedLegendrePolynomial class.
     *
     * @param l Degree of the associated Legendre polynomial
     * @param m Order of the associated Legendre polynomial
     */
    init {
        /* Make some basic, arithmetic checks. */

        require(m <= l) { "Associated Legendre Polynomials are defined for 0 <= m <= l. You provided m > l!" }
        require(m >= 0) { "Associated Legendre Polynomials are defined for 0 <= m <= l. You provided m < 0!" }
        require(l >= 0) { "Associated Legendre Polynomials are defined for 0 <= m <= l. You provided m < 0!" }

        /* Find m-th derivative of Legendre Polynomial of degree l. */
        var fkt: PolynomialFunction = PolynomialsUtils.createLegendrePolynomial(l)
        for (i in 0 until m) {
            fkt = fkt.polynomialDerivative()
        }
        this.legendre = fkt

        /* Determine sign. */
        this.sign = (-1.0).pow(m)
        this.m = m
        this.l = l
    }

    /**
     * Compute the value of the function.
     *
     * @param x Point at which the function value should be computed.
     * @return the value of the function.
     * @throws IllegalArgumentException when the activated method itself can ascertain that a precondition, specified in the API expressed at the level of the activated method, has been violated. When Commons Math throws an `IllegalArgumentException`, it is usually the consequence of checking the actual parameters passed to the method.
     */
    override fun value(x: Double): Double {
        return this.sign * (FastMath.pow(1.0 - FastMath.pow(x, 2.0), m / 2.0) * legendre.value(x))
    }
}