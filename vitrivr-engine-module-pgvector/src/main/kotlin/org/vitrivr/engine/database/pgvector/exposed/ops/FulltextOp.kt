package org.vitrivr.engine.database.pgvector.exposed.ops

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import org.vitrivr.engine.core.model.types.Value

/**
 * Match operator for [Value.String] values.
 */
class FulltextOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "@@") {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(expr1)
        queryBuilder.append(" ${this.opSign} to_tsquery(")
        queryBuilder.append(expr2)
        queryBuilder.append(")")
    }
}

