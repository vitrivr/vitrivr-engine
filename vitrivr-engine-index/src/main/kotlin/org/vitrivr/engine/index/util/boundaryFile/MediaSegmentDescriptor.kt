package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.model.relationship.Relationship

class MediaSegmentDescriptor(
    val objectId: String,
    val segmentId: String,
    val segmentNumber: Int,
    val start : Int,
    val end : Int,
    val startAbs: Double,
    val endAbs: Double,
    val exists: Boolean,
) {

}