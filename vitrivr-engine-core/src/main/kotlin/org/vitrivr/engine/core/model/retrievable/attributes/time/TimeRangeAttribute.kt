package org.vitrivr.engine.core.model.retrievable.attributes.time

import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.concurrent.TimeUnit

/**
 * A [RetrievableAttribute] encoding a timestamp.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TimeRangeAttribute(val startNs: Long, val endNs: Long) : RetrievableAttribute {
    constructor(start: Long, end: Long, unit: TimeUnit) : this(unit.toNanos(start), unit.toNanos(end))

    fun contains(timestamp: TimePointAttribute): Boolean = timestamp.timepointNs in this.startNs..this.endNs
}