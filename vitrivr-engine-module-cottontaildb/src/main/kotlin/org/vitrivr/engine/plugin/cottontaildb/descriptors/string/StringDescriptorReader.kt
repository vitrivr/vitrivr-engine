package org.vitrivr.engine.plugin.cottontaildb.descriptors.string

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.toValue
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader


/**
 * An [AbstractDescriptorReader] for [StringDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StringDescriptorReader(field: Schema.Field<*, StringDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<StringDescriptor>(field, connection) {
    override fun getAll(query: Query): Sequence<Retrieved> {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME)
        when (query) {
            is SimpleFulltextQuery -> {
                cottontailQuery.fulltext(DESCRIPTOR_COLUMN_NAME, query.value.value, "score")
                if (query.limit < Long.MAX_VALUE) {
                    cottontailQuery.limit(query.limit)
                }
            }

            is SimpleBooleanQuery<*> -> {
                require(query.value is Value.String) { "StringDescriptorReader can only perform comparisons to string values." }
                cottontailQuery.where(Compare(Column(this.entityName.column(DESCRIPTOR_COLUMN_NAME)), query.operator(), Literal(query.value.toCottontailValue())))
            }
            else -> throw IllegalArgumentException("Query of typ ${query::class} is not supported by StringDescriptorReader.")
        }

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map { tuple ->
            val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
            val score = tuple.asDouble(SCORE_COLUMN_NAME) ?: 0.0
            val retrieved = Retrieved(retrievableId, null, false)
            retrieved.addAttribute(ScoreAttribute(score))
            retrieved
        }
    }

    override fun tupleToDescriptor(tuple: Tuple): StringDescriptor {
        val descriptorId = tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        val value = tuple.asString(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
        return StringDescriptor(descriptorId, retrievableId, value)
    }
}