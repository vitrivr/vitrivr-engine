package org.vitrivr.engine.index.util.boundaryFile

interface MediaSegmentDecriptable {
    fun toMediaSegmentDescriptors(): List<MediaSegmentDescriptor>
}