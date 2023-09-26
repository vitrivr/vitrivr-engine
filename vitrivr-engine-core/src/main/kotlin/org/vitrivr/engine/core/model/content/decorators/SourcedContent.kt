package org.vitrivr.engine.core.model.content.decorators

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.source.Source

/**
 * A [Content] element that is connected and thus part of a particular [Source].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface SourcedContent: ContentDecorator {

    /** The [Source] the [Content] is */
    val source: Source

    /**
     * A [SourcedContent] that has a temporal location within the [Source].
     */
    interface Temporal: SourcedContent {
        /** The timepoint of the temporal location in nanoseconds. */
        val timepointNs: Long
    }

    /**
     * A [SourcedContent] that has a spatial (point) location within the [Source].
     */
    interface Point2D: SourcedContent {
        /** Position in X direction within the [Source]. */
        val x: Int

        /** Position in Y direction within the [Source]. */
        val y: Int
    }

    /**
     * A [SourcedContent] that has a spatial (area) location within the [Source].
     */
    interface Rectangle2D: SourcedContent {
        /** Position of the top-left point in X direction within the [Source]. */
        val leftX: Int

        /** Position of the top-left point in Y  direction within the [Source]. */
        val leftY: Int

        /** Width of the rectangle in pixels [Source]. */
        val width: Int

        /** Height of the rectangle in pixels [Source]. */
        val height: Int
    }

    /**
     * A [SourcedContent] that has a spatial location within the [Source]
     * (e.g., for 3D meshes).
     */
    interface Point3D: SourcedContent {
        /** Center position in X direction within the [Source]. */
        val centerX: Int

        /** Center position in Y direction within the [Source]. */
        val centerY: Int

        /** Center position in Z direction within the [Source].*/
        val centerZ: Int
    }
}