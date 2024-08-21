package org.vitrivr.engine.core.model.color.rgb

/**
 * A container for RGB colors.
 *
 * @version 1.1.0
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
@JvmInline
value class RGBByteColorContainer(private val rgb: Int) {
    /**
     * Constructor for creating a [RGBByteColorContainer] from RGB [UByte] values.
     *
     * @param red Red component.
     * @param green Green component.
     * @param blue Blue component.
     */
    constructor(red: UByte, green: UByte, blue: UByte) : this(blue.toInt() and 0xFF or ((green.toInt() and 0xFF) shl 8) or ((red.toInt() and 0xFF) shl 16))

    /** Red component of this [RGBByteColorContainer]. */
    val red: UByte
        get() = (this.rgb shr 16 and 0xFF).toUByte()

    /** Green component of this [RGBByteColorContainer]. */
    val green: UByte
        get() = (this.rgb shr 8 and 0xFF).toUByte()

    /** Blue component of this [RGBByteColorContainer]. */
    val blue: UByte
        get() = (this.rgb and 0xFF).toUByte()

    /**
     * Converts this [RGBByteColorContainer] to an [RGBFloatColorContainer].
     *
     * @return [RGBFloatColorContainer] representation of this [RGBByteColorContainer].
     */
    fun toFloatContainer(): RGBFloatColorContainer =
        RGBFloatColorContainer(this.red.toFloat() / 255f, this.green.toFloat() / 255f, this.blue.toFloat() / 255f)

    /**
     * Converts this [RGBByteColorContainer] to an [Int] representation.
     *
     * @return [Int] representation of this [RGBByteColorContainer].
     */
    fun toRGBInt(): Int = this.rgb
}
