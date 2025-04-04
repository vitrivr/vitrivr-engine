package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.model.relationship.Relationship
import java.time.Duration

class MediaSegmentDescriptor(
    val objectId: String,
    val segmentId: String,
    val segmentNumber: Int,
    val start : Int,
    val end : Int,
    val startAbs: Duration,
    val endAbs: Duration,
    val exists: Boolean,
) {

}