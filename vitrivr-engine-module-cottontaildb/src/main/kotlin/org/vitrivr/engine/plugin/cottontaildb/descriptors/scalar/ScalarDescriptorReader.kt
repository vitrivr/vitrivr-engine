package org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.toValue
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader

/**
 * An [AbstractDescriptorReader] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*, *>>, connection: CottontailConnection) : AbstractDescriptorReader<ScalarDescriptor<*, *>>(field, connection) {

    /** Prototype [ScalarDescriptor] used to create new instances. */
    private val prototype = this.field.analyser.prototype(this.field)

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun query(query: Query): Sequence<ScalarDescriptor<*, *>> = when (query) {
        is SimpleFulltextQuery -> this.queryFulltext(query)
        is SimpleBooleanQuery<*> -> this.queryBoolean(query)
        else -> throw UnsupportedOperationException("The provided query type ${query::class.simpleName} is not supported by this reader.")
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
            // @formatter:off
            is BooleanDescriptor -> BooleanDescriptor(descriptorId, retrievableId, tuple.asBoolean(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is DoubleDescriptor -> DoubleDescriptor(descriptorId, retrievableId, tuple.asDouble(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is FloatDescriptor -> FloatDescriptor(descriptorId, retrievableId, tuple.asFloat(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is ByteDescriptor -> ByteDescriptor(descriptorId, retrievableId, tuple.asByte(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is ShortDescriptor -> ShortDescriptor(descriptorId, retrievableId, tuple.asShort(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is IntDescriptor -> IntDescriptor(descriptorId, retrievableId, tuple.asInt(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is LongDescriptor -> LongDescriptor(descriptorId, retrievableId, tuple.asLong(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is StringDescriptor -> StringDescriptor(descriptorId, retrievableId, tuple.asString(VALUE_ATTRIBUTE_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            is TextDescriptor -> TextDescriptor(descriptorId, retrievableId, tuple.asString(VALUE_ATTRIBUTE_NAME)?.let { Value.Text(it) } ?: throw IllegalArgumentException("The provided tuple is missing the required field '$VECTOR_ATTRIBUTE_NAME'."))
            // @formatter:on
        }
    }

    /**
     * Executes a [SimpleFulltextQuery] and returns a [Sequence] of [ScalarDescriptor]s.
     *
     * @param query The [SimpleFulltextQuery] to execute.
     * @return [Sequence] of [ScalarDescriptor]s.
     */
    private fun queryFulltext(query: SimpleFulltextQuery): Sequence<ScalarDescriptor<*, *>> {
        val queryValue = query.value.value.split(" ").joinToString(" OR ", "(", ")") { "$it*" }
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .select("*")
            .fulltext(VALUE_ATTRIBUTE_NAME, queryValue, "score")

        if (query.limit < Long.MAX_VALUE) {
            cottontailQuery.limit(query.limit)
        }

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map {
            this.tupleToDescriptor(it)
        }
    }

    /**
     * Executes a [SimpleBooleanQuery] and returns a [Sequence] of [ScalarDescriptor]s.
     *
     * @param query The [SimpleBooleanQuery] to execute.
     * @return [Sequence] of [ScalarDescriptor]s.
     */
    private fun queryBoolean(query: SimpleBooleanQuery<*>): Sequence<ScalarDescriptor<*, *>> {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .select(RETRIEVABLE_ID_COLUMN_NAME)
            .select(DESCRIPTOR_ID_COLUMN_NAME)
            .select(VALUE_ATTRIBUTE_NAME)
            .where(Compare(Column(this.entityName.column(VALUE_ATTRIBUTE_NAME)), query.operator(), Literal(query.value.toCottontailValue())))

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map {
            this.tupleToDescriptor(it)
        }
    }
}
