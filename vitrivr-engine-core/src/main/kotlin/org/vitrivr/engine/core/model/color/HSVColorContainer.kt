package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.types.Value
import java.awt.color.ColorSpace

/**
 * A container for HSV colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class HSVColorContainer constructor(private val hsv: FloatArray) {

    init {
        require(this.hsv.size == 3) { "HSVFloatColorContainer must have exactly 3 elements." }
        require(this.hsv[0] in 0.0f..1.0f) { "HSVFloatColorContainer components must be between 0.0 and 1.0." }
        require(this.hsv[1] in 0.0f..1.0f) { "HSVFloatColorContainer components must be between 0.0 and 1.0." }
        require(this.hsv[2] in 0.0f..1.0f) { "HSVFloatColorContainer components must be between 0.0 and 1.0." }
    }

    constructor(hue: Float, saturation: Float, value: Float) : this(floatArrayOf(hue, saturation, value))
    constructor(hue: Double, saturation: Double, value: Double) : this(floatArrayOf(hue.toFloat(), saturation.toFloat(), value.toFloat()))

    /** Accessor for the hue component of the [HSVColorContainer]. */
    val hue: Float
        get() = this.hsv[0]

    /** Accessor for the saturation component of the [HSVColorContainer]. */
    val saturation: Float
        get() = this.hsv[1]

    /** Accessor for the value component of the [HSVColorContainer]. */
    val value: Float
        get() = this.hsv[2]

    /**
     * Converts this [HSVColorContainer] a [Value.FloatVector]
     *
     * @return [Value.FloatVector]
     */
    fun toVector() = Value.FloatVector(this.hsv)

    /**
     * Converts this [HSVColorContainer] to a [RGBColorContainer].
     *
     * @return [RGBColorContainer]
     */
    fun toRGB(): RGBColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_HSV)
        val hsv = space.toRGB(this.hsv)
        return RGBColorContainer(hsv[0], hsv[1], hsv[2])
    }

    override fun toString(): String = "HSVFloatColorContainer(H=$hue, S=$saturation, V=$value)"
}