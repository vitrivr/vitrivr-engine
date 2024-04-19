package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store spatial metadata in a 2D raster graphic.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class Rectangle2DMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val leftX: Value.Int,
    val leftY: Value.Int,
    val width: Value.Int,
    val height: Value.Int,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object {
        /** The field schema associated with a [Rectangle2DMetadataDescriptor]. */
        private val SCHEMA = listOf(
            FieldSchema("leftX", Type.INT),
            FieldSchema("leftY", Type.INT),
            FieldSchema("width", Type.INT),
            FieldSchema("height", Type.INT),
        )

        /** The prototype [Rectangle2DMetadataDescriptor]. */
        val PROTOTYPE = Rectangle2DMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Int(0), Value.Int(0), Value.Int(0), Value.Int(0))
    }

    /**
     * Returns the [FieldSchema] [List ]of this [StructDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and its values of this [Rectangle2DMetadataDescriptor] as a [Map].
     *
     * @return A [Map] of this [Rectangle2DMetadataDescriptor]'s fields (without the IDs).
     */
    override fun values(): List<Pair<String, Any?>> = listOf(
        "leftX" to this.leftX,
        "leftY" to this.leftY,
        "width" to this.width,
        "height" to this.height
    )
}