package org.vitrivr.engine.core.model.color.hsv

/**
 * A container for HSV colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class HSVColorContainer private constructor(private val colors: FloatArray) {

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
}