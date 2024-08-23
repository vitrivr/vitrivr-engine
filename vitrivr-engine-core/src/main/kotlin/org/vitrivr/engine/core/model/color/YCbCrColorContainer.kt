package org.vitrivr.engine.core.model.color

import java.awt.color.ColorSpace

/**
 * A container for YCbCr colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class YCbCrColorContainer constructor(private val ycbcr: FloatArray) {
    init {
        require(this.ycbcr.size == 3) { "YCbCrColorContainer must have exactly 3 elements." }
    }

    constructor(y: Float, cb: Float, cr: Float) : this(floatArrayOf(y, cb, cr))

    /** Accessor for the L component of the [LabColorContainer]. */
    val y: Float
        get() = this.ycbcr[0]

    /** Accessor for the A component of the [LabColorContainer]. */
    val cb: Float
        get() = this.ycbcr[1]

    /** Accessor for the B component of the [LabColorContainer]. */
    val cr: Float
        get() = this.ycbcr[2]

    /**
     * Converts this [YCbCrColorContainer] to a [RGBColorContainer].
     *
     * @return [RGBColorContainer]
     */
    fun toRGB(): RGBColorContainer {
        val space = ColorSpace.getInstance(ColorSpace.TYPE_YCbCr)
        val rgb = space.toRGB(this.ycbcr)
        return RGBColorContainer(rgb[0], rgb[1], rgb[2])
    }
}