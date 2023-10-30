package org.vitrivr.engine.core.model.content.decorators

import org.vitrivr.engine.core.source.Source

/**
 * A [ContentDecorator] that signifies a spatial 2D location.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Spatial2DContent {
    /**
     * A [Spatial2DContent] that has a spatial (point) location.
     */
    interface Point {
        /** Position in X direction within the [Source]. */
        val x: Int

        /** Position in Y direction within the [Source]. */
        val y: Int
    }

    /**
     * A [Spatial2DContent] that spans a spatial (area) location.
     */
    interface Rectangle : SourcedContent {
        /** Position of the top-left point in X direction within the [Source]. */
        val leftX: Int

        /** Position of the top-left point in Y  direction within the [Source]. */
        val leftY: Int

        /** Width of the rectangle in pixels [Source]. */
        val width: Int

        /** Height of the rectangle in pixels [Source]. */
        val height: Int
    }
}