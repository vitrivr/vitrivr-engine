package org.vitrivr.engine.core.model.content

import org.vitrivr.engine.core.source.Source

/**
 * A [Content] element extracted from some [Source].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Content {
    /** The [Source] of this [Content]. */
    val source: Source

    /** The timestamp in nanoseconds within the [Source]. May be null, if [Content] is not associated to a point in time. */
    val timeNs: Long?

    /** A position in X direction within the [Source]. May be null, if [Content] is not associated to a position in that dimension.*/
    val posX: Int?

    /** A position in Y direction within the [Source]. May be null, if [Content] is not associated to a position in that dimension.*/
    val posY: Int?

    /** A position in Z direction within the [Source]. May be null, if [Content] is not associated to a position in that dimension.*/
    val posZ: Int?
}