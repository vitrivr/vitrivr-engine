package org.vitrivr.engine.core.model.color

import kotlin.math.*

open class RGBFloatColorContainer(open val red: Float, open val green: Float, open val blue: Float) {

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
