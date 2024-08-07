package org.vitrivr.engine.model3d.lwjglrender.window

import java.io.Serializable


/**
 * This class holds all options for a window.
 */
open class WindowOptions : Serializable {
    /**
     * set to true if the window should be created with a compatible profile
     */
    var compatibleProfile: Boolean = false

    /**
     * Frames per second. If set to -1, the fps is unlimited.
     */
    var fps: Int = -1

    /**
     * Updates per second. If set to -1, the ups is unlimited.
     */
    var ups: Int = 30

    /**
     * The height of the window.
     */
    var height: Int = 400

    /**
     * The width of the window.
     */
    var width: Int = 400

    /**
     * Hide the window after creation.
     */
    var hideWindow: Boolean = true

    /**
     * Empty constructor for WindowOptions.
     */
    constructor()

    /**
     * Basic Constructor for WindowOptions.
     *
     * @param width  Width of the window.
     * @param height Height of the window.
     */
    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
    }
}