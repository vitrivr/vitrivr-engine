package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a video source (e.g., a file).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoSourceMetadataDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    val width: Value.Int,
    val height: Value.Int,
    val fps: Value.Double,
    val channels: Value.Int,
    val sampleRate: Value.Int,
    val sampleSize: Value.Int,
    override val field: Schema.Field<*, VideoSourceMetadataDescriptor>? = null
) : StructDescriptor {

    companion object {
        /** The field schema associated with a [VideoSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            FieldSchema("width", Type.INT),
            FieldSchema("height", Type.INT),
            FieldSchema("fps", Type.DOUBLE),
            FieldSchema("channels", Type.INT),
            FieldSchema("sampleRate", Type.INT),
            FieldSchema("sampleSize", Type.INT),
        )

        /** The prototype [VideoSourceMetadataDescriptor]. */
        val PROTOTYPE = VideoSourceMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Int(0), Value.Int(0), Value.Double(0.0), Value.Int(0), Value.Int(0), Value.Int(0))
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