package org.vitrivr.engine.model3d.lwjglrender.render

import org.joml.Vector4f
import java.io.Serializable
import java.util.function.Function

/**
 * RenderOptions
 *
 *  * Used to switch on or off the texture rendering
 *  * Used to switch on or off the coloring rendering
 *  * Returns the color for the given value
 *  * Can be used to colorize the model custom
 *
 */
open class RenderOptions : Serializable {
    /**
     * Used to switch on or off the texture rendering
     */
    public var showTextures: Boolean = true

    /**
     * Used to switch on or off the coloring rendering For future face coloring
     */
    @Suppress("unused")
    var showColor: Boolean = false

    /**
     * Returns the color for the given value Can be used to colorize the model custom
     *
     * @TODO: This cannot be serialized!
     */
    @Transient
    var colorfunction: Function<Float, Vector4f> =
        Function { v: Float? -> Vector4f(v!!, v, v, 1f) }
}