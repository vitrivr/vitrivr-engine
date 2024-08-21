package org.vitrivr.engine.core.model.color

import org.vitrivr.engine.core.model.color.hsv.HSVColorContainer
import org.vitrivr.engine.core.model.color.rgb.RGBByteColorContainer
import org.vitrivr.engine.core.model.color.rgb.RGBFloatColorContainer

/**
 * A color converter that provides methods to convert between different color spaces.
 *
 * Adapted from Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object ColorConverter {

    /**
     * Converts a [RGBByteColorContainer] to a [HSVColorContainer].
     *
     * @param rgb The [RGBByteColorContainer] to convert.
     * @return [HSVColorContainer]
     */
    fun rgbToHsv(rgb: RGBByteColorContainer): HSVColorContainer = rgbToHsv(rgb.toFloatContainer())

    /**
     * Converts a [RGBFloatColorContainer] to a [HSVColorContainer].
     *
     * @param rgb The [RGBFloatColorContainer] to convert.
     * @return [HSVColorContainer]
     */
    fun rgbToHsv(rgb: RGBFloatColorContainer): HSVColorContainer {
        val max = maxOf(rgb.red, rgb.green, rgb.blue)
        val min = minOf(rgb.red, rgb.green, rgb.blue)
        val d = max - min
        val s = if (max == 0f) 0f else d / max
        val h = when {
            d == 0f -> 0f
            max == rgb.red -> (rgb.green - rgb.blue) / d + (if (rgb.green < rgb.blue) 6 else 0)
            max == rgb.green -> (rgb.blue - rgb.red) / d + 2
            else -> (rgb.red - rgb.green) / d + 4
        }
        return HSVColorContainer(h, s, max)
    }
}