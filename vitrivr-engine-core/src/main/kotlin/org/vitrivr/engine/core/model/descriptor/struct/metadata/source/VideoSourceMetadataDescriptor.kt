package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a video source (e.g., a file).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoSourceMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val width: Int,
    val height: Int,
    val fps: Double,
    val channels: Int,
    val sampleRate: Int,
    val sampleSize: Int,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object {
        /** The field schema associated with a [VideoSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            FieldSchema("width", FieldType.INT),
            FieldSchema("height", FieldType.INT),
            FieldSchema("fps", FieldType.DOUBLE),
            FieldSchema("channels", FieldType.INT),
            FieldSchema("sampleRate", FieldType.INT),
            FieldSchema("sampleSize", FieldType.INT),
        )

        /** The prototype [VideoSourceMetadataDescriptor]. */
        val PROTOTYPE = VideoSourceMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), 0, 0, 0.0, 0, 0, 0)
    }

    /**
     * Returns the [FieldSchema] [List ]of this [StructDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and its values of this [FileSourceMetadataDescriptor] as a [Map].
     *
     * @return A [Map] of this [FileSourceMetadataDescriptor]'s fields (without the IDs).
     */
    override fun values(): List<Pair<String, Any?>> = listOf(
        "width" to this.width,
        "height" to this.height,
        "fps" to this.fps,
        "channels" to this.channels,
        "sampleRate" to this.sampleRate,
        "sampleSize" to this.sampleSize
    )
}