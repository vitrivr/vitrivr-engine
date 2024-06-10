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
 * @version 2.0.0
 */
class StringDescriptorReader(field: Schema.Field<*, StringDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<StringDescriptor>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun query(query: Query): Sequence<StringDescriptor> {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME)
        when (query) {
            is SimpleFulltextQuery -> {
                cottontailQuery
                    .select("*")
                    .fulltext(DESCRIPTOR_COLUMN_NAME, query.value.value, "score")
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
            this.tupleToDescriptor(tuple)
        }
    }

    /**
     * Returns a [Sequence] of all [Retrieved]s that match the given [Query].
     *
     * Implicitly, this methods executes a [query] and then JOINS the result with the [Retrieved]s.
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Retrieved].
     */
    override fun queryAndJoin(query: Query): Sequence<Retrieved> {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME)
        when (query) {
            is SimpleFulltextQuery -> {
                cottontailQuery
                    .select(DESCRIPTOR_COLUMN_NAME)
                    .select(DESCRIPTOR_ID_COLUMN_NAME)
                    .fulltext(DESCRIPTOR_COLUMN_NAME, query.value.value, "score")

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
        val descriptors = this.connection.client.query(cottontailQuery).asSequence().map { tuple ->
            val scoreIndex = tuple.indexOf(SCORE_COLUMN_NAME)
            tupleToDescriptor(tuple) to if (scoreIndex > -1) {
                tuple.asDouble(SCORE_COLUMN_NAME)?.let { ScoreAttribute.Unbound(it.toFloat()) }
            } else {
                null
            }
        }.toList()

        /* Fetch retrievable ids. */
        val retrievables = this.fetchRetrievable(descriptors.mapNotNull { it.first.retrievableId }.toSet())
        return descriptors.asSequence().mapNotNull { descriptor ->
            val retrievable = retrievables[descriptor.first.retrievableId] ?: return@mapNotNull null

            /* Append descriptor and score attribute. */
            retrievable.addDescriptor(descriptor.first)
            descriptor.second?.let { retrievable.addAttribute(it) }
            retrievable
        }
    }

    override fun tupleToDescriptor(tuple: Tuple): StringDescriptor {
        val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        val descriptorId = tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        val value = tuple.asString(DESCRIPTOR_COLUMN_NAME)?.toValue() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
        return StringDescriptor(descriptorId, retrievableId, value)
    }
}
