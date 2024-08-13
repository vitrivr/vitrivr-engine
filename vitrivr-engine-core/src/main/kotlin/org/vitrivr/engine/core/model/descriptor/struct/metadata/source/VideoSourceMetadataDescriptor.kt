package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
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
 * @version 2.0.0
 */
class VideoSourceMetadataDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, VideoSourceMetadataDescriptor>? = null,
) : MapStructDescriptor(id, retrievableId, SCHEMA, values, field) {

    /** The width of the video source in pixels. */
    val width: Value.Int by this.values

    /** The width of the video source in pixels. */
    val height: Value.Int by this.values

    /** The number of visual frames per seconds. */
    val fps: Value.Double by this.values

    /** The number of audio channels. */
    val channels: Value.Int by this.values

    /** The sample rate in kHz. */
    val sampleRate: Value.Int by this.values

    /** The sample size in bytes. */
    val sampleSize: Value.Int by this.values

    companion object {
        /** The field schema associated with a [VideoSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            Attribute("width", Type.Int),
            Attribute("height", Type.Int),
            Attribute("fps", Type.Double),
            Attribute("channels", Type.Int),
            Attribute("sampleRate", Type.Int),
            Attribute("sampleSize", Type.Int),
        )

        /** The prototype [VideoSourceMetadataDescriptor]. */
        val PROTOTYPE = VideoSourceMetadataDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            mapOf(
                "width" to Value.Int(0),
                "height" to Value.Int(0),
                "fps" to Value.Double(0.0),
                "channels" to Value.Int(0),
                "sampleRate" to Value.Int(0),
                "sampleSize" to Value.Int(0)
            )
        )
    }
}