package org.vitrivr.engine.plugin.cottontaildb.descriptors.struct

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * An [AbstractDescriptorReader] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class StructDescriptorReader(field: Schema.Field<*, StructDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<StructDescriptor>(field, connection) {

    /** An internal [Map] that maps field name to Cottontail DB [Types]. */
    private val fieldMap = mutableListOf<Pair<String, Types<*>>>()
    init {
        val prototype = this.field.analyser.prototype(this.field)
        for (f in prototype.schema()) {
            require(f.dimensions.size <= 1) { "Cottontail DB currently doesn't support tensor types."}
            this.fieldMap.add(f.name to f.toCottontailType())
        }
    }

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query): Sequence<Retrieved> {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME)
        when (query) {
            is SimpleFulltextQuery -> {
                require(query.attributeName != null) { "Fulltext query on a struct field requires specification of a field's attribute name." }
                cottontailQuery.fulltext(query.attributeName!!, query.value.value, SCORE_COLUMN_NAME)
                if (query.limit < Long.MAX_VALUE) {
                    cottontailQuery.limit(query.limit)
                }
            }

            is SimpleBooleanQuery<*> -> {
                require(query.attributeName != null){"Boolean query on a struct field requires specification of a field's attribute name."}
                cottontailQuery.where(Compare(Column(this.entityName.column(query.attributeName!!)), query.operator(), Literal(query.value.toCottontailValue())))
            }
            else -> throw IllegalArgumentException("Query of typ ${query::class} is not supported by StringDescriptorReader.")
        }

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map { tuple ->
            val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
            val score = tuple.asDouble(SCORE_COLUMN_NAME) ?: 0.0
            val retrieved = Retrieved(retrievableId, null, false)
            retrieved.addAttribute(ScoreAttribute.Unbound(score.toFloat()))
            retrieved
        }
    }

    /**
     * Converts the provided [Tuple] to a [StructDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [StructDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): StructDescriptor {
        val constructor = this.field.analyser.descriptorClass.primaryConstructor ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val parameters: MutableList<Any?> = mutableListOf(
            tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'."),
            tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'."),
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
