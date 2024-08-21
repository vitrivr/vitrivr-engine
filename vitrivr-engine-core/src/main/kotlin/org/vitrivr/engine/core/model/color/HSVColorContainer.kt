package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import kotlin.math.floor

/**
 * A container for HSV colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class HSVColorContainer constructor(private val colors: FloatArray) {

    constructor(hue: Float, saturation: Float, value: Float) : this(floatArrayOf(hue, saturation, value))
    constructor(hue: Double, saturation: Double, value: Double) : this(floatArrayOf(hue.toFloat(), saturation.toFloat(), value.toFloat()))

    /** Accessor for the hue component of the [HSVColorContainer]. */
    val hue: Float
        get() = this.colors[0]

    /** Accessor for the saturation component of the [HSVColorContainer]. */
    val saturation: Float
        get() = this.colors[1]

    /** Accessor for the value component of the [HSVColorContainer]. */
    val value: Float
        get() = this.colors[2]

    /**
     * Converts this [HSVColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.colors)

    /**
     * Converts this [HSVColorContainer] to a [RGBColorContainer].
     *
     * @return [RGBColorContainer]
     */
    fun toRGB(): RGBColorContainer {
        var r = 0
        var g = 0
        var b = 0
        if (this.saturation < 0.00001f) {
            b = (this.value * 255f).toInt()
            g = b
            r = g
        } else {
            val h: Double = (this.hue * 6.0) % 6.0
            val i = floor(h).toInt()
            val v1 = (this.value * (1.0 - this.saturation) * 255f).toInt()
            val v2 = (this.value * (1.0 - this.saturation * (h - i)) * 255f).toInt()
            val v3 = (this.value * (1.0 - this.saturation * (1 - (h - i))) * 255f).toInt()

            when (i) {
                0 -> {
                    r = (255f * this.value).toInt()
                    g = v3
                    b = v1
                }

                1 -> {
                    r = v2
                    g = (255f * this.value).toInt()
                    b = v1
                }

                2 -> {
                    r = v1
                    g = (255f * this.value).toInt()
                    b = v3
                }

                3 -> {
                    r = v1
                    g = v2
                    b = (this.value * 255f).toInt()
                }

                4 -> {
                    r = v3
                    g = v1
                    b = (this.value * 255f).toInt()
                }

                else -> {
                    r = (255f * this.value).toInt()
                    g = v1
                    b = v2
                }
            }
        }
        return RGBColorContainer(r, g, b)
    }

    override fun toString(): String = "HSVFloatColorContainer(H=$hue, S=$saturation, V=$value)"
}