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
     * A [SourcedContent] that has a spatial location within the [Source].
     */
    sealed interface Point2D: SourcedContent {
        /** A position in X direction within the [Source]. */
        val posX: Int

        /** A position in Y direction within the [Source]. */
        val posY: Int
    }

    /**
     * A [SourcedContent] that has a spatial location within the [Source].
     */
    sealed interface Point3D: SourcedContent {
        /** A position in X direction within the [Source]. */
        val posX: Int

        /** A position in Y direction within the [Source]. */
        val posY: Int

        /** A position in Z direction within the [Source].*/
        val posZ: Int
    }
}