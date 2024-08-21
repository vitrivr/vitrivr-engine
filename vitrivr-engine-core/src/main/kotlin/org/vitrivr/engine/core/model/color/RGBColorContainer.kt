package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A container for RGB colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class RGBColorContainer constructor(private val rgb: FloatArray) {

    init {
        require(this.rgb.size == 4) { "RGBFloatColorContainer must have exactly 3 elements." }
        require(this.rgb[0] in 0.0f..1.0f) { "RGBFloatColorContainer components must be between 0.0 and 1.0." }
        require(this.rgb[1] in 0.0f..1.0f) { "RGBFloatColorContainer components must be between 0.0 and 1.0." }
        require(this.rgb[2] in 0.0f..1.0f) { "RGBFloatColorContainer components must be between 0.0 and 1.0." }
        require(this.rgb[3] in 0.0f..1.0f) { "RGBFloatColorContainer components must be between 0.0 and 1.0." }

    }

    constructor(rgb: Int) : this((rgb shr 16 and 0xFF).toByte(), (rgb shr 8 and 0xFF).toByte(), (rgb and 0xFF).toByte())
    constructor(red: Byte, green: Byte, blue: Byte, alpha: Byte = Byte.MAX_VALUE) : this(red.toFloat() / 255f, green.toFloat() / 255f, blue.toFloat() / 255f, alpha.toFloat() / 255f)
    constructor(red: Int, green: Int, blue: Int, alpha: Int = 255) : this(red.toFloat() / 255f, green.toFloat() / 255f, blue.toFloat() / 255f, alpha.toFloat() / 255f)
    constructor(r: Double, g: Double, b: Double, alpha: Double = 1.0) : this(r.toFloat(), g.toFloat(), b.toFloat(), alpha.toFloat())
    constructor(red: Float, green: Float, blue: Float, alpha: Float = 1.0f) : this(floatArrayOf(red, green, blue, alpha))

    /** Accessor for the red component of the [RGBColorContainer]. */
    val red: Float
        get() = this.rgb[0]

    /** Accessor for the green component of the [RGBColorContainer]. */
    val green: Float
        get() = this.rgb[1]

    /** Accessor for the blue component of the [RGBColorContainer]. */
    val blue: Float
        get() = this.rgb[2]

    /** Accessor for the alpha component of the [RGBColorContainer]. */
    val alpha: Float
        get() = this.rgb[3]

    /**
     * Adds this [RGBColorContainer] to another [RGBColorContainer].
     *
     * @param other [RGBColorContainer] to add.
     * @return [RGBColorContainer]
     */
    operator fun plus(other: RGBColorContainer): RGBColorContainer =
        RGBColorContainer(this.red + other.red, this.green + other.green, this.blue + other.blue)

    /**
     * Subtracts a [RGBColorContainer] from this [RGBColorContainer].
     *
     * @param other [RGBColorContainer] to subtract.
     * @return [RGBColorContainer]
     */
    operator fun minus(other: RGBColorContainer): RGBColorContainer = RGBColorContainer(
        this.red - other.red,
        this.green - other.green,
        this.blue - other.blue
    )

    /**
     * Clamps the values of each color channel to [0f, 1f]
     */
    fun clamped(): RGBColorContainer = RGBColorContainer(min(1f, max(0f, this.red)), min(1f, max(0f, this.green)), min(1f, max(0f, this.blue)))

    /**
     * Normalizes the magnitude of the color vector to 1
     */
    fun normalized(): RGBColorContainer {
        val magnitude = sqrt(this.red * this.red + this.green * this.green + this.blue * this.blue)
        return if (magnitude > 0f) {
            RGBColorContainer(this.red / magnitude, this.green / magnitude, this.blue / magnitude)
        } else {
            this
        }
    }

    /**
     * Converts this [RGBColorContainer] to an [Int] representation.
     *
     * @return [Int] representation of RGB color
     */
    fun toRGBInt(): Int = (this.blue * 255).toInt() and 0XFF or ((this.green * 255).toInt() and 0xFF shl 8) or (((this.red * 255).toInt() and 0xFF) shl 16)

    /**
     * Converts this [RGBColorContainer] to a [HSVColorContainer].
     *
     * @return [HSVColorContainer]
     */
    fun toHSV(): HSVColorContainer {
        val max = maxOf(this.red, this.green, this.blue)
        val min = minOf(this.red, this.green, this.blue)
        val d = max - min
        val s = if (max == 0f) 0f else d / max
        val h = when {
            d == 0f -> 0f
            max == this.red -> (this.green - this.blue) / d + (if (this.green < this.blue) 6 else 0)
            max == this.green -> (this.blue - this.red) / d + 2
            else -> (this.red - this.green) / d + 4
        }
        return HSVColorContainer(h, s, max)
    }

    /**
     * Converts this [RGBColorContainer] to a [XYZColorContainer].
     *
     * @return [XYZColorContainer]
     */
    fun toXYZ(): XYZColorContainer {
        var r = this.red.toDouble()
        var g = this.green.toDouble()
        var b = this.blue.toDouble()
        if (r > 0.04045) {
            r = ((r + 0.055) / 1.055).pow(2.4)
        } else {
            r /= 12.92
        }

        if (g > 0.04045) {
            g = ((g + 0.055) / 1.055).pow(2.4)
        } else {
            g /= 12.92
        }

        if (b > 0.04045) {
            b = ((b + 0.055) / 1.055).pow(2.4)
        } else {
            b /= 12.92
        }

        r *= 100.0
        g *= 100.0
        b *= 100.0

        return XYZColorContainer(
            r * 0.4121 + g * 0.3576 + b * 0.1805,
            r * 0.2126 + g * 0.7152 + b * 0.0722,
            r * 0.0193 + g * 0.1192 + b * 0.9505
        )
    }

    fun toYCbCr(): YCbCrColorContainer {
        val y = Math.round((this.red * 255.0f * 65.738f + this.green * 255.0f * 129.057f + this.blue * 255.0f * 25.064f) / 256f + 16)
        val cb = Math.round((this.red * 255.0f * -37.945f + this.green * 255.0f * -74.494f + this.blue * 255.0f * 112.439f) / 256f + 128)
        val cr = Math.round((this.red * 255.0f * 112.439f + this.green * 255.0f * -94.154f + this.blue * 255.0f * -18.285f) / 256f + 128)
        return YCbCrColorContainer(y, cb, cr)
    }

    /**
     * Converts this [RGBColorContainer] as a [List] of [Float]s.
     *
     * @return [List] of [Float]s
     */
    fun toList() = listOf(this.red, this.green, this.blue)

    /**
     * Converts this [RGBColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.rgb)

    /**
     * Calculates distance to another [RGBColorContainer].
     *
     * @param other [RGBColorContainer] to calculate distance to.
     * @return Distance to [other].
     */
    fun distanceTo(other: RGBColorContainer): Float = sqrt(
        (this.red - other.red) * (this.red - other.red) + (this.green - other.green) * (this.green - other.green) + (this.blue - other.blue) * (this.blue - other.blue)
    )

    override fun toString(): String = "RGBFloatColorContainer(R=$red, G=$green, B=$blue)"
}
