package org.vitrivr.engine.index.util.boundary

interface MediaSegmentDecriptable {
    fun toMediaSegmentDescriptors(): List<MediaSegmentDescriptor>
}