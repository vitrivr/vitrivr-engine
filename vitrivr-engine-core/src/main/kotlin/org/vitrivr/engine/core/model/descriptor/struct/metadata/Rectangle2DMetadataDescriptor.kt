package org.vitrivr.engine.core.model.descriptor.struct.metadata

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
 * A [StructDescriptor] used to store spatial metadata in a 2D raster graphic.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
class Rectangle2DMetadataDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, Rectangle2DMetadataDescriptor>? = null
) : MapStructDescriptor(id, retrievableId, SCHEMA, values, field) {

    companion object {
        /** The field schema associated with a [Rectangle2DMetadataDescriptor]. */
        private val SCHEMA = listOf(
            Attribute("leftX", Type.Int),
            Attribute("leftY", Type.Int),
            Attribute("width", Type.Int),
            Attribute("height", Type.Int),
        )

        /** The prototype [Rectangle2DMetadataDescriptor]. */
        val PROTOTYPE = Rectangle2DMetadataDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            mapOf(
                "leftX" to Value.Int(0),
                "leftY" to Value.Int(0),
                "width" to Value.Int(0),
                "height" to Value.Int(0)
            )
        )
    }

    /** The top left pixel of the rectangle (X-component). */
    val leftX: Value.Int by this.values

    /** The top left pixel of the rectangle (Y-component). */
    val leftY: Value.Int by this.values

    /** The width of the rectangle. */
    val width: Value.Int by this.values

    /** The height of the rectangle. */
    val height: Value.Int by this.values
}