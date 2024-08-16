package org.vitrivr.engine.module.features.feature.averagecolorraster

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
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
) : MapStructDescriptor<RasterDescriptor>(id, retrievableId, LAYOUT, values, field) {

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

    /**
     * Returns a copy of this [RasterDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [RasterDescriptor].
     * @param retrievableId [RetrievableId] of the new [RasterDescriptor].
     * @param field [Schema.Field] the new [RasterDescriptor] belongs to.
     * @return Copy of this [RasterDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, RasterDescriptor>?) = RasterDescriptor(id, retrievableId, this.values, field)
}