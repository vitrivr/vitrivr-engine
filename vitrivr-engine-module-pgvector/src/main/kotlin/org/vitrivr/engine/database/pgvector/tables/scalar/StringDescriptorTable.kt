package org.vitrivr.engine.database.pgvector.tables.scalar

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.stringParam
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.exposed.functions.plainToTsQuery
import org.vitrivr.engine.database.pgvector.exposed.ops.tsMatches

/**
 * Table definition for the [StringDescriptor] entity.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
internal class StringDescriptorTable(field: Schema.Field<*, StringDescriptor>): AbstractScalarDescriptorTable<StringDescriptor, Value.String, String>(field) {
    override val descriptor = varchar("descriptor", 255)

    init {
        this.initializeIndexes()
    }

    /**
     * Converts a [ResultRow] to a [StringDescriptor].
     *
     * @param row The [ResultRow] to convert.
     * @return The [StringDescriptor] represented by the [ResultRow].
     */
    override fun rowToDescriptor(row: ResultRow) = StringDescriptor(
        id = row[id].value,
        retrievableId = row[retrievableId].value,
        value = Value.String(row[descriptor]),
        this.field
    )

    /**
     * Converts a [org.vitrivr.engine.core.model.query.Query] into a [Query] that can be executed against the database.
     *
     * @param query The [org.vitrivr.engine.core.model.query.Query] to convert.
     * @return The [Query] that can be executed against the database.
     * @throws UnsupportedOperationException If the query is not supported.
     */
    override fun parse(query: org.vitrivr.engine.core.model.query.Query): Query = when(query) {
        is SimpleBooleanQuery<*> -> this.parse(query)
        is SimpleFulltextQuery -> this.parse(query)
        else -> throw UnsupportedOperationException("Unsupported query type: ${query::class.simpleName}")
    }

    /**
     * Converts a [SimpleFulltextQuery] into a [Query] that can be executed against the database.
     *
     * @param query [SimpleFulltextQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    private fun parse(query: SimpleFulltextQuery): Query = this.selectAll().where {
        descriptor tsMatches plainToTsQuery(stringParam(query.value.value))
    }

    /**
     * Converts a [SimpleBooleanQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [SimpleBooleanQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    override fun parse(query: SimpleBooleanQuery<*>): Query = this.selectAll().where {
        val value = query.value.value as? String ?: throw IllegalArgumentException("Failed to execute query on ${nameInDatabaseCase()}. Comparison value of wrong type.")
        when(query.comparison) {
            ComparisonOperator.EQ -> descriptor eq value
            ComparisonOperator.NEQ -> descriptor neq value
            ComparisonOperator.LE -> descriptor less value
            ComparisonOperator.GR -> descriptor greater value
            ComparisonOperator.LEQ -> descriptor lessEq value
            ComparisonOperator.GEQ -> descriptor greaterEq value
            ComparisonOperator.LIKE -> descriptor like value
        }
    }
}