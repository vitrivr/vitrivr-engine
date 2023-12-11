package org.vitrivr.engine.base.database.cottontail.descriptors.label

import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorReader
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrieved
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * An [AbstractDescriptorReader] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorReader(field: Schema.Field<*, StructDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<StructDescriptor>(field, connection) {

    /** An internal [Map] that maps field name to Cottontail DB [Types]. */
    private val fieldMap = mutableListOf<Pair<String, Types<*>>>()
    init {
        val prototype = this.field.analyser.prototype()
        for (f in prototype.schema()) {
            require(f.dimensions.size <= 1) { "Cottontail DB currently doesn't support tensor types."}
            val vector = f.dimensions.size == 1
            val type = when (f.type) {
                FieldType.STRING -> Types.String
                FieldType.BYTE -> Types.Byte
                FieldType.SHORT -> Types.Short
                FieldType.BOOLEAN -> if (vector) { Types.BooleanVector(f.dimensions[0]) } else { Types.Boolean }
                FieldType.INT -> if (vector) { Types.IntVector(f.dimensions[0]) } else { Types.Int }
                FieldType.LONG -> if (vector) { Types.LongVector(f.dimensions[0]) } else { Types.Long }
                FieldType.FLOAT -> if (vector) { Types.FloatVector(f.dimensions[0]) } else { Types.Float }
                FieldType.DOUBLE -> if (vector) { Types.DoubleVector(f.dimensions[0]) } else { Types.Double }
            }
            this.fieldMap.add(f.name to type)
        }
    }

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved.WithDescriptor]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query<StructDescriptor>): Sequence<Retrieved> = throw UnsupportedOperationException("Query of type ${query::class} is not supported by StructDescriptorReader.")

    /**
     * Converts the provided [Tuple] to a [StructDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [StructDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): StructDescriptor {
        val constructor = this.field.analyser.descriptorClass.primaryConstructor ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val parameters: MutableList<Any?> = mutableListOf(
            tuple.asUuidValue(CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME}'."),
            tuple.asUuidValue(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME}'."),
        )

        /* Append dynamic parameters of struct. */
        for ((name, type) in this.fieldMap) {
            parameters.add(
                when(type) {
                    Types.Boolean -> tuple.asBoolean(name)
                    Types.Date -> tuple.asDate(name)
                    Types.Byte -> tuple.asByte(name)
                    Types.Double -> tuple.asDouble(name)
                    Types.Float ->  tuple.asFloat(name)
                    Types.Int -> tuple.asInt(name)
                    Types.Long -> tuple.asLong(name)
                    Types.Short -> tuple.asShort(name)
                    Types.String -> tuple.asString(name)
                    Types.Uuid -> UUID.fromString(tuple.asString(name))
                    is Types.BooleanVector -> tuple.asBooleanVector(name)
                    is Types.DoubleVector -> tuple.asBooleanVector(name)
                    is Types.FloatVector -> tuple.asBooleanVector(name)
                    is Types.IntVector -> tuple.asIntVector(name)
                    is Types.LongVector -> tuple.asLongVector(name)
                    is Types.ShortVector -> tuple.asShort(name)
                    else -> throw IllegalArgumentException("Type $type is not supported by StructDescriptorReader.")
                }
            )
        }

        parameters.add(false) //add 'transient' flag to false, since the results were actually retrieved

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }
}