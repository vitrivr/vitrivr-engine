package org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.model.types.toValue
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader

/**
 * An [AbstractDescriptorReader] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorReader<ScalarDescriptor<*>>(field, connection) {

    /** Prototype [ScalarDescriptor] used to create new instances. */
    private val prototype = this.field.analyser.prototype(this.field)

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query): Sequence<Retrieved> {
        require(query is SimpleBooleanQuery<*>) { "Query of type ${query::class} is not supported by ScalarDescriptorReader." }

        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .select(RETRIEVABLE_ID_COLUMN_NAME)
            .select(DESCRIPTOR_ID_COLUMN_NAME)
            .select(DESCRIPTOR_COLUMN_NAME)
            .where(Compare(Column(this.entityName.column(DESCRIPTOR_COLUMN_NAME)), query.operator(), Literal(query.value.toCottontailValue())))

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().mapNotNull {
            val descriptor = this.tupleToDescriptor(it)
            if (descriptor.retrievableId != null) {
                val retrieved = Retrieved(descriptor.retrievableId!!, null, false)
                retrieved.addAttribute(DescriptorAttribute(descriptor))
                retrieved
            } else {
                null
            }
        }
    }

    /**
     * Converts the provided [Tuple] to a [ScalarDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [ScalarDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): ScalarDescriptor<*> {
        val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        val descriptorId = tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        return when (this.prototype) {
            is BooleanDescriptor -> BooleanDescriptor(retrievableId, descriptorId, tuple.asBoolean(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."))
            is DoubleDescriptor -> DoubleDescriptor(retrievableId, descriptorId, tuple.asDouble(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."))
            is FloatDescriptor -> FloatDescriptor(retrievableId, descriptorId, tuple.asFloat(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."))
            is IntDescriptor -> IntDescriptor(retrievableId, descriptorId, tuple.asInt(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."))
            is LongDescriptor -> LongDescriptor(retrievableId, descriptorId, tuple.asLong(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."))
            is StringDescriptor -> StringDescriptor(retrievableId, descriptorId, tuple.asString(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."))
        }
    }
}