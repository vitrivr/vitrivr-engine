package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import java.awt.color.ColorSpace

/**
 * A container for LAB colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class LabColorContainer constructor(private val lab: FloatArray) {
    init {
        require(this.lab.size == 3) { "LabColorContainer must have exactly 3 elements." }
    }

    constructor(l: Float, a: Float, b: Float) : this(floatArrayOf(l, a, b))
    constructor(l: Double, a: Double, b: Double) : this(floatArrayOf(l.toFloat(), a.toFloat(), b.toFloat()))

    /** Accessor for the L component of the [LabColorContainer]. */
    val l: Float
        get() = this.lab[0]

    /** Accessor for the A component of the [LabColorContainer]. */
    val a: Float
        get() = this.lab[1]

    /** Accessor for the B component of the [LabColorContainer]. */
    val b: Float
        get() = this.lab[2]

    /**
     * Converts this [LabColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.lab)

    /**
     * Converts this [XYZColorContainer] to a [RGBColorContainer].
     *
     * @return [RGBColorContainer]
     */
    fun toRGB(): RGBColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_Lab)
        val lab = space.toRGB(this.lab)
        return RGBColorContainer(lab[0], lab[1], lab[2])
    }

    override fun toString(): String = "LabFloatColorContainer(L=$l, A=$a, B=$b)"
}