package org.vitrivr.engine.database.pgvector.exposed.ops

import org.jetbrains.exposed.sql.*

/**
 * A [ComparisonOp] for the distance operator `<->` in PostgreSQL.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed class DistanceOps(expr1: Expression<*>, expr2: Expression<*>, name: String) : CustomOperator<Float>(name, FloatColumnType(), expr1, expr2) {
    class Euclidean(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<->")
    class Manhattan(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<+>")
    class Cosine(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<=>") {
        override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit = queryBuilder {
            append('(', '1', ' ', '-', ' ', '(', expr1, ' ', operatorName, ' ', expr2, ')', ')')
        }
    }
    class Inner(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<#>")
    class Hamming(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<~>")
    class Jaccard(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<%>")
}
