package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import java.awt.color.ColorSpace

/**
 * A container for XYZ colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class XYZColorContainer(private val xyz: FloatArray) {

    constructor(x: Float, y: Float, z: Float) : this(floatArrayOf(x, y, z))
    constructor(x: Double, y: Double, z: Double) : this(floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat()))

    /** Accessor for the L component of the [LabColorContainer]. */
    val x: Float
        get() = this.xyz[0]

    /** Accessor for the A component of the [LabColorContainer]. */
    val y: Float
        get() = this.xyz[1]

    /** Accessor for the B component of the [LabColorContainer]. */
    val z: Float
        get() = this.xyz[2]

    /**
     * Converts this [XYZColorContainer] to a [RGBColorContainer].
     *
     * @return [RGBColorContainer]
     */
    fun toRGB(): RGBColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_XYZ)
        val xyz = space.toRGB(this.xyz)
        return RGBColorContainer(xyz[0], xyz[1], xyz[2])
    }

    /**
     * Converts this [XYZColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.xyz)

    override fun toString(): String {
        return "XYZFloatColorContainer(X=$x, Y=$y, Z=$z)"
    }
}