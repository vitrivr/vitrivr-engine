package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

data class MediaDimensionsDescriptor(
        override val id: DescriptorId,
        override val retrievableId: RetrievableId,
        val width: Int,
        val height: Int,
        override val transient: Boolean = false
) : StructDescriptor {

        companion object {
            private val SCHEMA = listOf(
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
        * Returns the fields and its values of this [MediaDimensionsDescriptor] as a [Map].
        *
        * @return A [Map] of this [MediaDimensionsDescriptor]'s fields (without the IDs).
        */
        override fun values(): List<Pair<String, Any?>> = listOf(
                "width" to this.width,
                "height" to this.height
        )
}