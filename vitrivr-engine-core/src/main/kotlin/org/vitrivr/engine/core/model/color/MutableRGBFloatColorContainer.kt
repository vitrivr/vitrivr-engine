package org.vitrivr.engine.core.model.color

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class MutableRGBFloatColorContainer(override var red: Float, override var green: Float, override var blue: Float) :
    RGBFloatColorContainer(red, green, blue) {

    override operator fun plus(other: RGBFloatColorContainer): MutableRGBFloatColorContainer =
        MutableRGBFloatColorContainer(this.red + other.red, this.green + other.green, this.blue + other.blue)

    override operator fun plus(other: RGBByteColorContainer): MutableRGBFloatColorContainer =
        MutableRGBFloatColorContainer(
            this.red + (other.red.toFloat() / 255f),
            this.green + (other.green.toFloat() / 255f),
            this.blue + (other.blue.toFloat() / 255f)
        )

    override operator fun minus(other: RGBFloatColorContainer): MutableRGBFloatColorContainer =
        MutableRGBFloatColorContainer(this.red - other.red, this.green - other.green, this.blue - other.blue)

    override operator fun minus(other: RGBByteColorContainer): MutableRGBFloatColorContainer =
        MutableRGBFloatColorContainer(
            this.red - (other.red.toFloat() / 255f),
            this.green - (other.green.toFloat() / 255f),
            this.blue - (other.blue.toFloat() / 255f)
        )

    /**
     * Clamps the values of each color channel to [0f, 1f]
     */
    override fun clamped(): MutableRGBFloatColorContainer =
        MutableRGBFloatColorContainer(
            min(1f, max(0f, this.red)),
            min(1f, max(0f, this.green)),
            min(1f, max(0f, this.blue))
        )

    /**
     * Clamps the values of each color channel to [0f, 1f] in this instance
     */
    fun clamp(): MutableRGBFloatColorContainer {
        this.red = min(1f, max(0f, this.red))
        this.green = min(1f, max(0f, this.green))
        this.blue = min(1f, max(0f, this.blue))
        return this
    }

    /**
     * Normalizes the magnitude of the color vector to 1
     */
    override fun normalized(): MutableRGBFloatColorContainer {
        val magnitude = sqrt(this.red * this.red + this.green * this.green + this.blue * this.blue)
        return if (magnitude > 0f) {
            MutableRGBFloatColorContainer(this.red / magnitude, this.green / magnitude, this.blue / magnitude)
        } else {
            this
        }
    }

    /**
     * Normalizes the magnitude of this color vector to 1
     */
    fun normalize(): MutableRGBFloatColorContainer {
        val magnitude = sqrt(this.red * this.red + this.green * this.green + this.blue * this.blue)
        if (magnitude > 0f) {
            this.red /= magnitude
            this.green /= magnitude
            this.blue /= magnitude
        }
        return this
    }

}
