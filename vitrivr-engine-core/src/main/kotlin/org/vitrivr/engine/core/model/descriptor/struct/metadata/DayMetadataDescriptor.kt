package org.vitrivr.engine.core.model.descriptor.struct.metadata

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store date time information; specifically, the day and the derived dayOfWeek
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
data class DayMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val day: Value.DateTime,
    val dayOfWeek: Value.String,
    override val field: Schema.Field<*, DayMetadataDescriptor>? = null
) : StructDescriptor {

    companion object {
        /** The key of the field `day` */
        const val DAY_FIELD_KEY = "day"

        /** The key of the field `dayOfWeek` */
        const val DAY_OF_WEEK_FIELD_KEY = "dayOfWeek"

        /** The field schema associated with a [DayMetadataDescriptor] */
        private val SCHEMA = listOf(
            FieldSchema(DAY_FIELD_KEY, Type.DATETIME),
            FieldSchema(DAY_OF_WEEK_FIELD_KEY, Type.STRING),
        )

        /** The prototypical [DayMetadataDescriptor] */
        val PROTOTYPE = DayMetadataDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Value.DateTime(Date()),
            Value.String("")
        )

    }

    /**
     * Returns the [FieldSchema] list of this [DayMetadataDescriptor]
     *
     * @return [List] of [FieldSchema], the schema of this [StructDescriptor]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and their values of this [DayMetadataDescriptor] as a [List] of [Pair]s
     *
     * @return A [List] of [Pair]s, named tuples of this [DayMetadataDescriptor]'s fields and their values (excluding the IDs).
     */
    override fun values(): List<Pair<String, Any?>> = listOf(
        DAY_FIELD_KEY to this.day,
        DAY_OF_WEEK_FIELD_KEY to this.dayOfWeek
    )
}
