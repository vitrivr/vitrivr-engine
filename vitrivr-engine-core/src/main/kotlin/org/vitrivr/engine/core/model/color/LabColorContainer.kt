package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value

/**
 * A container for LAB colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class LabColorContainer constructor(private val colors: FloatArray) {

    constructor(l: Float, a: Float, b: Float) : this(floatArrayOf(l, a, b))
    constructor(l: Double, a: Double, b: Double) : this(floatArrayOf(l.toFloat(), a.toFloat(), b.toFloat()))

    /** Accessor for the L component of the [LabColorContainer]. */
    val l: Float
        get() = this.colors[0]

    /** Accessor for the A component of the [LabColorContainer]. */
    val a: Float
        get() = this.colors[1]

    /** Accessor for the B component of the [LabColorContainer]. */
    val b: Float
        get() = this.colors[2]

    /**
     * Converts this [LabColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.colors)

    override fun toString(): String = "LabFloatColorContainer(L=$l, A=$a, B=$b)"
}