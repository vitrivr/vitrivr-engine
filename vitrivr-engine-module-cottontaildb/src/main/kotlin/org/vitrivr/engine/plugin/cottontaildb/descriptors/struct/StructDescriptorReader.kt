package org.vitrivr.engine.plugin.cottontaildb.descriptors.struct

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader
import java.time.ZoneOffset
import kotlin.reflect.full.primaryConstructor

/**
 * An [AbstractDescriptorReader] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class StructDescriptorReader(field: Schema.Field<*, StructDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorReader<StructDescriptor<*>>(field, connection) {

    /** An internal [Map] that maps field name to Cottontail DB [Types]. */
    private val fieldMap = mutableListOf<Pair<String, Types<*>>>()
    init {
        val prototype = this.field.analyser.prototype(this.field)
        for (f in prototype.layout()) {
            this.fieldMap.add(f.name to f.type.toCottontailType())
        }
    }

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<StructDescriptor<*>> = when (query) {
        is SimpleFulltextQuery -> this.queryFulltext(query)
        is SimpleBooleanQuery<*> -> this.queryBoolean(query)
        else -> throw UnsupportedOperationException("The provided query type ${query::class.simpleName} is not supported by this reader.")
    }

    /**
     * Converts the provided [Tuple] to a [StructDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [StructDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): StructDescriptor<*> {
        val constructor = this.field.analyser.descriptorClass.primaryConstructor ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val valueMap = mutableMapOf<AttributeName, Value<*>>()
        val parameters: MutableList<Any?> = mutableListOf(
            tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'."),
            tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'."),
            valueMap
        )

        /* Append dynamic parameters of struct. */
        for ((name, type) in this.fieldMap) {
            valueMap[name] = when (type) {
                Types.Boolean -> tuple.asBoolean(name)?.let { Value.Boolean(it) }
                Types.Date -> tuple.asDate(name)?.let { Value.DateTime(it.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime()) }
                Types.Byte -> tuple.asByte(name)?.let { Value.Byte(it) }
                Types.Double -> tuple.asDouble(name)?.let { Value.Double(it) }
                Types.Float -> tuple.asFloat(name)?.let { Value.Float(it) }
                Types.Int -> tuple.asInt(name)?.let { Value.Int(it) }
                Types.Long -> tuple.asLong(name)?.let { Value.Long(it) }
                Types.Short -> tuple.asShort(name)?.let { Value.Short(it) }
                Types.String -> tuple.asString(name)?.let { Value.String(it) }
                Types.Uuid -> tuple.asUuidValue(name)?.let { Value.UUIDValue(it.value) }
                is Types.BooleanVector -> tuple.asBooleanVector(name)?.let { Value.BooleanVector(it) }
                is Types.DoubleVector -> tuple.asDoubleVector(name)?.let { Value.DoubleVector(it) }
                is Types.FloatVector -> tuple.asFloatVector(name)?.let { Value.FloatVector(it) }
                is Types.IntVector -> tuple.asIntVector(name)?.let { Value.IntVector(it) }
                is Types.LongVector -> tuple.asLongVector(name)?.let { Value.LongVector(it) }
                else -> throw IllegalArgumentException("Type $type is not supported by StructDescriptorReader.")
            } as Value<*>
        }

        parameters.add(field) //add field information, as this is for all StructDescriptors the last constructor argument.

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }

    /**
     * Executes a [SimpleFulltextQuery] and returns a [Sequence] of [StructDescriptor]s.
     *
     * @param query The [SimpleFulltextQuery] to execute.
     * @return [Sequence] of [StructDescriptor]s.
     */
    private fun queryFulltext(query: SimpleFulltextQuery): Sequence<StructDescriptor<*>> {
        require(query.attributeName != null) { "Fulltext query on a struct field requires specification of a field's attribute name." }
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME).select(DESCRIPTOR_ID_COLUMN_NAME)
        for ((name, _) in this.fieldMap) {
            cottontailQuery.select(name)
        }

        val queryValue = query.value.value.split(" ").joinToString(" OR ", "(", ")") { "$it*" }
        cottontailQuery.fulltext(query.attributeName!!, queryValue, SCORE_COLUMN_NAME)
        if (query.limit < Long.MAX_VALUE) {
            cottontailQuery.limit(query.limit)
        }

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map {
            this.tupleToDescriptor(it)
        }
    }

    /**
     * Executes a [SimpleBooleanQuery] and returns a [Sequence] of [StructDescriptor]s.
     *
     * @param query The [SimpleBooleanQuery] to execute.
     * @return [Sequence] of [StructDescriptor]s.
     */
    private fun queryBoolean(query: SimpleBooleanQuery<*>): Sequence<StructDescriptor<*>> {
        require(query.attributeName != null) { "Boolean query on a struct field requires specification of a field's attribute name." }
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME).select(DESCRIPTOR_ID_COLUMN_NAME)
        for ((name, _) in this.fieldMap) {
            cottontailQuery.select(name)
        }
        cottontailQuery.where(Compare(Column(query.attributeName!!), query.operator(), Literal(query.value.toCottontailValue())))

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map {
            this.tupleToDescriptor(it)
        }
    }
}
