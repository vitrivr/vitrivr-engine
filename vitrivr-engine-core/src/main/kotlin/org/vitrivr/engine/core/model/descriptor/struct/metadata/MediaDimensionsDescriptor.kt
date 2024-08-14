package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a 2D raster graphic.
 *
 * @author Ralph Gasser
 * @version 2.1.0
 */
class MediaDimensionsDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, MediaDimensionsDescriptor>? = null
) : MapStructDescriptor(id, retrievableId, SCHEMA, values, field) {
    companion object {
        /** The field schema associated with a [VideoSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            Attribute("width", Type.Int),
            Attribute("height", Type.Int),
        )

        /** The prototype [MediaDimensionsDescriptor]. */
        val PROTOTYPE = MediaDimensionsDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            mapOf(
                "width" to Value.Int(0),
                "height" to Value.Int(0)
            )
        )
    }

    /** The width of this image. */
    val width: Value.Int by this.values

    /** The height of this image. */
    val height: Value.Int by this.values

    /**
     * Returns a copy of this [MediaDimensionsDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [MediaDimensionsDescriptor].
     * @param retrievableId [RetrievableId] of the new [MediaDimensionsDescriptor].
     * @return Copy of this [MediaDimensionsDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = MediaDimensionsDescriptor(id, retrievableId, HashMap(this.values), this.field)
}