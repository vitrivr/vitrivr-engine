package org.vitrivr.engine.core.model.content.decorators

/**
 * A [ContentDecorator] that signifies a temporal location.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface TemporalContent : ContentDecorator {

    /** A [TemporalContent] that represents a point in time. */
    interface Timepoint {
        /** The timepoint of the temporal location in nanoseconds. */
        val timepointNs: Long
    }

    /** A [TemporalContent] that represents a time span- */
    interface TimeSpan {
        /** The start timepoint of the temporal location in nanoseconds. */
        val startNs: Long

        /** The end timepoint of the temporal location in nanoseconds. */
        val endNs: Long
    }
}