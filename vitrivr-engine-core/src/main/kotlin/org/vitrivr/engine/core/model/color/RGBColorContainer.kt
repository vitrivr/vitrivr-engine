package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import java.awt.color.ColorSpace
import kotlin.math.max
import kotlin.math.min
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
        require(this.rgb.size == 4) { "RGBFloatColorContainer must have exactly 4 elements." }
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
        val space = ColorSpace.getInstance(ColorSpace.TYPE_HSV)
        return HSVColorContainer(space.fromRGB(this.rgb))
    }

    /**
     * Converts this [RGBColorContainer] to a [LabColorContainer].
     *
     * @return [LabColorContainer]
     */
    fun toLab(): LabColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_Lab)
        return LabColorContainer(space.fromRGB(this.rgb))
    }

    /**
     * Converts this [RGBColorContainer] to a [XYZColorContainer].
     *
     * @return [XYZColorContainer]
     */
    fun toXYZ(): XYZColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_XYZ)
        return XYZColorContainer(space.fromRGB(this.rgb))
    }

    /**
     * Converts this [RGBColorContainer] to a [YCbCrColorContainer].
     *
     * @return [YCbCrColorContainer]
     */
    fun toYCbCr(): YCbCrColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_YCbCr)
        return YCbCrColorContainer(space.fromRGB(this.rgb))
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
