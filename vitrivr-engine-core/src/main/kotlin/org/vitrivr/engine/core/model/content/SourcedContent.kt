package org.vitrivr.engine.core.model.content

import org.vitrivr.engine.core.source.Source

/**
 * A [Content] element that is connected to a particular [Source].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface SourcedContent: Content {

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
    sealed interface Spatial: SourcedContent {

        /**
         * A [SourcedContent.Spatial] that is tied to a particular point.
         */
        interface Point {
            /** A position in X direction within the [Source]. */
            val posX: Int

            /** A position in Y direction within the [Source]. */
            val posY: Int

            /** A position in Z direction within the [Source].*/
            val posZ: Int
        }
    }
}