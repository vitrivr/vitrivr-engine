package org.vitrivr.engine.base.database.cottontail.descriptors.string

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.*
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.string.TextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved


/**
 * An [AbstractDescriptorReader] for [StringDescriptor]s.
 *
 * @author Fynn Faber
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StringDescriptorReader(field: Schema.Field<*, StringDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<StringDescriptor>(field, connection) {
    override fun getAll(query: Query<StringDescriptor>): Sequence<Retrieved> {
        /* Prepare query. */
        val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).select(RETRIEVABLE_ID_COLUMN_NAME)

        when (query) {
            is TextQuery -> cottontailQuery.fulltext(DESCRIPTOR_COLUMN_NAME, query.descriptor.value, "score")
            is BooleanQuery<StringDescriptor> -> cottontailQuery.where(Compare(Column(this.entityName.column(DESCRIPTOR_COLUMN_NAME)), query.operator(), Literal(query.descriptor.toValue())))
            else -> throw IllegalArgumentException("Query of typ ${query::class} is not supported by StringDescriptorReader.")
        }

        /* Execute query. */
        return this.connection.client.query(cottontailQuery).asSequence().map { tuple ->
            val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
            val score = tuple.asDouble(SCORE_COLUMN_NAME) ?: 0.0
            Retrieved.WithScore(retrievableId, null, score.toFloat(), false)
        }
    }

    override fun tupleToDescriptor(tuple: Tuple): StringDescriptor {
        val descriptorId = tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        val value = tuple.asString(DESCRIPTOR_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
        return StringDescriptor(descriptorId, retrievableId, value)
    }
}