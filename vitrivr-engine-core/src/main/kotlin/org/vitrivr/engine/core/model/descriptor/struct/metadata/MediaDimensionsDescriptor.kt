package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a 2D raster graphic.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class MediaDimensionsDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val width: Value.Int,
    val height: Value.Int,
    override val transient: Boolean = false
) : StructDescriptor {

        companion object {
            /** The field schema associated with a [VideoSourceMetadataDescriptor]. */
            private val SCHEMA = listOf(
                FieldSchema("width", Type.INT),
                FieldSchema("height", Type.INT),
            )

            /** The prototype [MediaDimensionsDescriptor]. */
            val PROTOTYPE = MediaDimensionsDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Int(0), Value.Int(0))
        }

        /**
        * Returns the [FieldSchema] [List ]of this [StructDescriptor].
        *
        * @return [List] of [FieldSchema]
        */
        override fun schema(): List<FieldSchema> = SCHEMA

        /**
        * Returns the fields and its values of this [MediaDimensionsDescriptor] as a [Map].
        *
        * @return A [Map] of this [MediaDimensionsDescriptor]'s fields (without the IDs).
        */
        override fun values(): List<Pair<String, Any?>> = listOf(
            "width" to this.width,
            "height" to this.height
        )
}