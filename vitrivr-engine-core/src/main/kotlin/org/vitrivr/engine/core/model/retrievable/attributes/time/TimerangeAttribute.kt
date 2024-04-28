package org.vitrivr.engine.core.model.retrievable.attributes.time

import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import java.util.concurrent.TimeUnit

/**
 * A [RetrievableAttribute] encoding a timestamp.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TimerangeAttribute(val startNs: Long, val endNs: Long) : RetrievableAttribute {
    constructor(start: Long, end: Long, unit: TimeUnit) : this(unit.toNanos(start), unit.toNanos(end))

    fun contains(timestamp: TimestampAttribute): Boolean = timestamp.timestampNs in this.startNs..this.endNs
}