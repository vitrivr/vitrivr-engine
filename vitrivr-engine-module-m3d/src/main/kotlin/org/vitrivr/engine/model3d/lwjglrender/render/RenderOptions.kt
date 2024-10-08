package org.vitrivr.engine.model3d.lwjglrender.render

import org.vitrivr.engine.core.model.mesh.texturemodel.util.types.Vec4f
import java.io.Serializable
import java.util.function.Function

/**
 * RenderOptions
 *
 *  * Used to switch on or off the texture rendering
 *  * Used to switch on or off the coloring rendering
 *  * Returns the color for the given value
 *  * Can be used to colorize the model custom
 */
data class RenderOptions(
    /**
     * Used to switch on or off the texture rendering
     */
    var showTextures: Boolean = true,

    /**
     * Used to switch on or off the coloring rendering For future face coloring
     */
    var showColor: Boolean = false,

    /**
     * Returns the color for the given value Can be used to colorize the model custom
     *
     * @TODO: This cannot be serialized!
     */
    @Transient
    var colorfunction: Function<Float, Vec4f> = Function { v: Float? -> Vec4f(v!!, v, v, 1f) }
) : Serializable
