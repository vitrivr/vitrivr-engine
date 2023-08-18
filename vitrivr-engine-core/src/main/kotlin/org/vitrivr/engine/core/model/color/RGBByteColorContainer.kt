package org.vitrivr.engine.core.model.color

data class RGBByteColorContainer(val red: UByte, val green: UByte, val blue: UByte) {

    companion object {
        fun fromRGB(rgb: Int): RGBByteColorContainer =
            RGBByteColorContainer(
                (rgb shr 16 and 0xFF).toUByte(),
                (rgb shr 8 and 0xFF).toUByte(),
                (rgb and 0xFF).toUByte()
            )
    }

    fun toFloatContainer(): RGBFloatColorContainer =
        RGBFloatColorContainer(this.red.toFloat() / 255f, this.green.toFloat() / 255f, this.blue.toFloat() / 255f)

    fun toRGBInt(): Int = this.blue.toInt() and 0xFF or ((this.green.toInt() and 0xFF) shl 8) or ((this.red.toInt() and 0xFF) shl 16)

}
