package org.vitrivr.engine.model3d.lwjglrender.scene

import org.joml.Matrix4f

/**
 * The Projection class is used to create a projection matrix.
 * The projection matrix is used to transform the 3D scene into a 2D image.
 */
class Projection(width: Int, height: Int) {
    /**
     * Returns the projection matrix.
     *
     * @return The projection matrix.
     */
    /**
     * The projMatrix for rendering the scene.
     */
    val projMatrix: Matrix4f = Matrix4f()

    /**
     * Initializes the Projection with the given width and height.
     * Creates a new projection matrix.
     *
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    init {
        this.updateProjMatrix(width, height)
    }

    /**
     * Updates the projection matrix.
     *
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    fun updateProjMatrix(width: Int, height: Int) {
        projMatrix.setPerspective(FOV, width.toFloat() / height.toFloat(), Z_NEAR, Z_FAR)
    }

    companion object {
        /**
         * The FOV is the field of view of the camera.
         */
        private val FOV = Math.toRadians(60.0).toFloat()

        /**
         * The Z_FAR and Z_NEAR values are used to set the clipping planes.
         */
        private const val Z_FAR = 100f

        /**
         * The Z_FAR and Z_NEAR values are used to set the clipping planes.
         */
        private const val Z_NEAR = 0.01f
    }
}