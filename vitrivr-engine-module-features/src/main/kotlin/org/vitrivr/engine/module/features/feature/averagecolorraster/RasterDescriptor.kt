package org.vitrivr.engine.module.features.feature.averagecolorraster

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A struct vector descriptor that stores the average color and raster of an image.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class RasterDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, RasterDescriptor>? = null
) : MapStructDescriptor(id, retrievableId, LAYOUT, values, field) {

    companion object {
        private val LAYOUT = listOf(
            Attribute("hist", Type.FloatVector(15)),
            Attribute("raster", Type.FloatVector(4))
        )
    }

    /** The histogram vector. */
    val hist: Value.FloatVector by this.values

    /** The raster vector. */
    val raster: Value.FloatVector by this.values
}