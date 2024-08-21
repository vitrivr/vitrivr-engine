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
        if (this.saturation < 0.00001f) {
            return RGBColorContainer(this.value, this.value, this.value)
        } else {
            val h: Float = ((this.hue * 6.0) % 6.0).toFloat()
            val i = floor(h).toInt()
            val v1 = (this.value * (1.0f - this.saturation))
            val v2 = (this.value * (1.0f - this.saturation * (h - i)))
            val v3 = (this.value * (1.0f - this.saturation * (1.0f - (h - i))))
            return when (i) {
                0 -> RGBColorContainer(this.value, v3, v1)
                1 -> RGBColorContainer(v2, this.value, v1)
                2 -> RGBColorContainer(v1, this.value, v3)
                3 -> RGBColorContainer(v1, v2, 1.0f)
                4 -> RGBColorContainer(v3, v1, this.value)
                else -> RGBColorContainer(this.value, v1, v2)
            }
        }
    }

    override fun toString(): String = "HSVFloatColorContainer(H=$hue, S=$saturation, V=$value)"
}