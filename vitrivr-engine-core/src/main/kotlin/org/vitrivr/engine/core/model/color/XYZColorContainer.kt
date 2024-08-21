package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import kotlin.math.pow

/**
 * A container for XYZ colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class XYZColorContainer constructor(private val colors: FloatArray) {

    constructor(x: Float, y: Float, z: Float) : this(floatArrayOf(x, y, z))
    constructor(x: Double, y: Double, z: Double) : this(floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat()))

    /** Accessor for the L component of the [LabColorContainer]. */
    val x: Float
        get() = this.colors[0]

    /** Accessor for the A component of the [LabColorContainer]. */
    val y: Float
        get() = this.colors[1]

    /** Accessor for the B component of the [LabColorContainer]. */
    val z: Float
        get() = this.colors[2]

    /**
     * Converts this [XYZColorContainer] to a [LabColorContainer].
     *
     * @return [LabColorContainer]
     */
    fun toLab(): LabColorContainer {
        var x = this.x / 95.047
        var y = this.y / 100.0
        var z = this.z / 108.883

        if (x > 0.008856f) {
            x = x.pow((1.0 / 3.0))
        } else {
            x = (x * 7.787 + (16.0 / 116.0))
        }

        y = if (y > 0.008856f) {
            y.pow((1.0 / 3.0))
        } else {
            (y * 7.787 + (16.0 / 116.0))
        }

        z = if (z > 0.008856f) {
            z.pow((1.0 / 3.0))
        } else {
            (z * 7.787 + (16.0 / 116.0))
        }

        return LabColorContainer((116.0 * y) - 16.0, 500.0 * (x - y), 200.0 * (y - z))
    }

    /**
     * Converts this [XYZColorContainer] to a [RGBColorContainer].
     *
     * @return [LabColorContainer]
     */
    fun toRGB(): RGBColorContainer {
        val x: Double = this.x / 100.0
        val y: Double = this.y / 100.0
        val z: Double = this.z / 100.0

        var r = x * 3.2406 + y * -1.5372 + z * -0.4986
        var g = x * -0.9689 + y * 1.8758 + z * 0.0415
        var b = x * 0.0557 + y * -0.2040 + z * 1.0570

        if (r > 0.0031308) {
            r = 1.055 * r.pow((1.0 / 2.4)) - 0.055
        } else {
            r *= 12.92
        }

        if (g > 0.0031308) {
            g = 1.055 * g.pow((1.0 / 2.4)) - 0.055
        } else {
            g *= 12.92
        }

        if (b > 0.0031308) {
            b = 1.055 * b.pow((1.0 / 2.4)) - 0.055
        } else {
            b *= 12.92
        }

        return RGBColorContainer(r, g, b)
    }

    /**
     * Converts this [XYZColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.colors)

    override fun toString(): String {
        return "XYZFloatColorContainer(X=$x, Y=$y, Z=$z)"
    }
}