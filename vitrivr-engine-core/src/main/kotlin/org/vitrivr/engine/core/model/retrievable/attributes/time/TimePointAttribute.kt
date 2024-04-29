package org.vitrivr.engine.core.model.retrievable.attributes.time

import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.concurrent.TimeUnit

/**
 * A [RetrievableAttribute] encoding a timestamp.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TimePointAttribute(val timepointNs: Long) : RetrievableAttribute, Comparable<TimePointAttribute> {
    constructor(timestamp: Long, unit: TimeUnit) : this(unit.toNanos(timestamp))

    override fun compareTo(other: TimePointAttribute): Int = this.timepointNs.compareTo(other.timepointNs)
}