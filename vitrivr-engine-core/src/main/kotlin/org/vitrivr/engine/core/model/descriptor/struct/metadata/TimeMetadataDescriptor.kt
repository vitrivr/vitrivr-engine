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
 * A [StructDescriptor] used to store date time information; specifically, the time and the phaseOfDay (i.e. morning, noon, afternoon)
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
data class TimeMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val time: Value.DateTime,
    val phaseOfDay: Value.String,
    override val field: Schema.Field<*, TimeMetadataDescriptor>?=null
): StructDescriptor {

    companion object {
        const val TIME_FIELD_KEY = "time"
        const val PHASE_OF_DAY_FIELD_KEY = "phaseOfDay"

        /** The field schema associated with a [TimeMetadataDescriptor] */
        private val SCHEMA = listOf(
            FieldSchema(TIME_FIELD_KEY, Type.DATETIME),
            FieldSchema(PHASE_OF_DAY_FIELD_KEY, Type.STRING),
        )

        /** The prototypical [TimeMetadataDescriptor] */
        val PROTOTYPE = TimeMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.DateTime(Date()), Value.String(""))
    }

    /**
     * Returns a  [FieldSchema] [List] for this [TimeMetadataDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    /**
     * Returns the fields and its values of this [TimeMetadataDescriptor] as a [List] of [Pair]s.
     *
     * @return A [List] of this [TimeMetadataDescriptor]'s fields as key-value tuples (without the IDs).
     */
    override fun values(): List<Pair<String, Any?>> = listOf(
        TIME_FIELD_KEY to time,
        PHASE_OF_DAY_FIELD_KEY to phaseOfDay,
    )
}
