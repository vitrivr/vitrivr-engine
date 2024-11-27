package org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.expression.ValueList
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.Comparison
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextPredicate
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.toValue
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader

/**
 * An [AbstractDescriptorReader] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*, *>>, connection: CottontailConnection) : AbstractDescriptorReader<ScalarDescriptor<*, *>>(field, connection) {

    /** Prototype [ScalarDescriptor] used to create new instances. */
    private val prototype = this.field.analyser.prototype(this.field)

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [ScalarDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<ScalarDescriptor<*, *>> {
        val cottontailQuery = when (val predicate = query.predicate) {
            is SimpleFulltextPredicate -> this.queryFulltext(predicate)
            is Comparison<*> -> this.queryBoolean(predicate)
            else -> throw UnsupportedOperationException("The provided query type ${query::class.simpleName} is not supported by this reader.")
        }

        /* Apply limit (if defined). */
        if (query.limit < Long.MAX_VALUE) {
            cottontailQuery.limit(query.limit)
        }

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map {
            this.tupleToDescriptor(it)
        }
    }

    /**
     * Converts the provided [Tuple] to a [ScalarDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [ScalarDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): ScalarDescriptor<*, *> {
        val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        val descriptorId = tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        return when (this.prototype) {
            is BooleanDescriptor -> BooleanDescriptor(descriptorId, retrievableId, tuple.asBoolean(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is DoubleDescriptor -> DoubleDescriptor(descriptorId, retrievableId, tuple.asDouble(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is FloatDescriptor -> FloatDescriptor(descriptorId, retrievableId, tuple.asFloat(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is ByteDescriptor -> ByteDescriptor(descriptorId, retrievableId, tuple.asByte(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is ShortDescriptor -> ShortDescriptor(descriptorId, retrievableId, tuple.asShort(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is IntDescriptor -> IntDescriptor(descriptorId, retrievableId, tuple.asInt(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is LongDescriptor -> LongDescriptor(descriptorId, retrievableId, tuple.asLong(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is StringDescriptor -> StringDescriptor(descriptorId, retrievableId, tuple.asString(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is TextDescriptor -> TextDescriptor(descriptorId, retrievableId, tuple.asString(VALUE_ATTRIBUTE_NAME)?.let { Value.Text(it) } ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
        }
    }

    /**
     * Prepares a [SimpleFulltextPredicate] and returns a [org.vitrivr.cottontail.client.language.dql.Query].
     *
     * @param query The [SimpleFulltextPredicate] to execute.
     * @return  [org.vitrivr.cottontail.client.language.dql.Query]
     */
    private fun queryFulltext(query: SimpleFulltextPredicate): org.vitrivr.cottontail.client.language.dql.Query {
        val queryValue = query.value.value.split(" ").joinToString(" OR ", "(", ")") { "$it*" }
        return org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .select("*")
            .fulltext(VALUE_ATTRIBUTE_NAME, queryValue, "score")
    }

    /**
     * Prepares a [Comparison] and returns a [org.vitrivr.cottontail.client.language.dql.Query].
     *
     * @param query The [Comparison] to execute.
     * @return [org.vitrivr.cottontail.client.language.dql.Query]
     */
    private fun queryBoolean(query: Comparison<*>): org.vitrivr.cottontail.client.language.dql.Query {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .select(RETRIEVABLE_ID_COLUMN_NAME)
            .select(DESCRIPTOR_ID_COLUMN_NAME)
            .select(VALUE_ATTRIBUTE_NAME)

        /* Apply where-clause. */
        return if (query is Comparison.In<*>) {
            cottontailQuery.where(Compare(Column(this.entityName.column(VALUE_ATTRIBUTE_NAME)), Compare.Operator.IN, ValueList(query.values.map { it.toCottontailValue() }.toTypedArray())))
        } else {
            cottontailQuery.where(Compare(Column(this.entityName.column(VALUE_ATTRIBUTE_NAME)), query.operator(), Literal(query.toCottontailValue())))
        }
    }
}
