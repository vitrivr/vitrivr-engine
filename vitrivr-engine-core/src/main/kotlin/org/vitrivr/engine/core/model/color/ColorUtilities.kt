package org.vitrivr.engine.core.model.color

/**
 * Utility class for color calculations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object ColorUtilities {
    /**
     * Calculates the average color of a collection of [RGBColorContainer]s.
     *
     * @param colors The [Collection] of [RGBColorContainer]s for which the average should be calculated.
     * @return Average [RGBColorContainer].
     */
    fun avg(colors: Collection<RGBColorContainer>): RGBColorContainer {
        var r = 0.0f
        var g = 0.0f
        var b = 0.0f
        var alpha = 0.0f
        var len = 0.0f
        for (color in colors) {
            alpha = color.alpha
            r += color.red * color.alpha
            g += color.green * color.alpha
            b += color.blue * color.alpha
            len += color.alpha
        }
        return if (len < 1f) {
            RGBColorContainer(1.0f, 1.0f, 1.0f)
        } else {
            RGBColorContainer(r / len, g / len, b / len, alpha / len)
        }
    }
}