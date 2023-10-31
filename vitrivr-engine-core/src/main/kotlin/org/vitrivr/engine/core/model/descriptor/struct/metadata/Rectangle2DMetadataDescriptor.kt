package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store temporal metadata about a [Retrievable].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class Rectangle2DMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val leftX: Int,
    val leftY: Int,
    val width: Int,
    val height: Int,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object {
        private val SCHEMA = listOf(
            FieldSchema("leftX", FieldType.INT),
            FieldSchema("leftY", FieldType.INT),
            FieldSchema("width", FieldType.INT),
            FieldSchema("height", FieldType.INT),

            )
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
    override fun values(): Map<String, Any?> = mapOf(
        "leftX" to this.leftX,
        "leftY" to this.leftY,
        "width" to this.width,
        "height" to this.height
    )
}