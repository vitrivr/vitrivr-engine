package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

data class RasterDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val hist: List<Value.Float>,
    val raster: List<Value.Float>,
    override val transient: Boolean = false
) : StructDescriptor {


    /**
     * Returns the [FieldSchema] [List ]of this [RasterDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = listOf(
        FieldSchema("hist", Type.FLOAT, intArrayOf(this.hist.size)),
        FieldSchema("raster", Type.FLOAT, intArrayOf(this.raster.size))
    )

    override fun values(): List<Pair<String, Any?>> = listOf(
            "hist" to this.hist,
            "raster" to this.raster
    )
}