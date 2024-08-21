package org.vitrivr.engine.core.model.color.rgb

import org.vitrivr.engine.core.model.types.Value
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

open class RGBFloatColorContainer(open val red: Float, open val green: Float, open val blue: Float) {

    constructor(floats: List<Float>) : this(floats[0], floats[1], floats[2])

    open operator fun plus(other: RGBFloatColorContainer): RGBFloatColorContainer =
        RGBFloatColorContainer(this.red + other.red, this.green + other.green, this.blue + other.blue)

    open operator fun plus(other: RGBByteColorContainer): RGBFloatColorContainer = RGBFloatColorContainer(
        this.red + (other.red.toFloat() / 255f),
        this.green + (other.green.toFloat() / 255f),
        this.blue + (other.blue.toFloat() / 255f)
    )

    open operator fun minus(other: RGBFloatColorContainer): RGBFloatColorContainer =
        RGBFloatColorContainer(this.red - other.red, this.green - other.green, this.blue - other.blue)

    open operator fun minus(other: RGBByteColorContainer): RGBFloatColorContainer =
        RGBFloatColorContainer(
            this.red - (other.red.toFloat() / 255f),
            this.green - (other.green.toFloat() / 255f),
            this.blue - (other.blue.toFloat() / 255f)
        )

    /**
     * Clamps the values of each color channel to [0f, 1f]
     */
    open fun clamped(): RGBFloatColorContainer =
        RGBFloatColorContainer(min(1f, max(0f, this.red)), min(1f, max(0f, this.green)), min(1f, max(0f, this.blue)))

    /**
     * Normalizes the magnitude of the color vector to 1
     */
    open fun normalized(): RGBFloatColorContainer {
        val magnitude = sqrt(this.red * this.red + this.green * this.green + this.blue * this.blue)
        return if (magnitude > 0f) {
            RGBFloatColorContainer(this.red / magnitude, this.green / magnitude, this.blue / magnitude)
        } else {
            this
        }
    }

    fun toMutable(): MutableRGBFloatColorContainer = MutableRGBFloatColorContainer(this.red, this.green, this.blue)

    fun toByteContainer(): RGBByteColorContainer = RGBByteColorContainer(
        (min(1f, max(0f, this.red)) * 255f).roundToInt().toUByte(),
        (min(1f, max(0f, this.green)) * 255f).roundToInt().toUByte(),
        (min(1f, max(0f, this.blue)) * 255f).roundToInt().toUByte()
    )

    /**
     * Converts this [RGBFloatColorContainer] as a [List] of [Float]s.
     *
     * @return [List] of [Float]s
     */
    fun toList() = listOf(this.red, this.green, this.blue)

    /**
     * Converts this [RGBFloatColorContainer] as a [List] of [Value.Float]s.
     *
     * @return [List] of [Value.Float]s
     */
    fun toVector() = Value.FloatVector(floatArrayOf(this.red, this.green, this.blue))

    fun distanceTo(other: RGBFloatColorContainer): Float = sqrt(
        (this.red - other.red) * (this.red - other.red) + (this.green - other.green) * (this.green - other.green) + (this.blue - other.blue) * (this.blue - other.blue)
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RGBFloatColorContainer) return false

        if (red != other.red) return false
        if (green != other.green) return false
        if (blue != other.blue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = red.hashCode()
        result = 31 * result + green.hashCode()
        result = 31 * result + blue.hashCode()
        return result
    }

    override fun toString(): String {
        return "RGBFloatColorContainer(red=$red, green=$green, blue=$blue)"
    }


}
