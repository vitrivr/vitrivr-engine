package org.vitrivr.engine.database.pgvector.exposed.ops

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.CustomOperator
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.FloatColumnType

/**
 * A [ComparisonOp] for the distance operator `<->` in PostgreSQL.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed class DistanceOps(expr1: Expression<*>, expr2: Expression<*>, name: String) : CustomOperator<Float>(name, FloatColumnType(), expr1, expr2) {
    class Euclidean(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<->")
    class Manhattan(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<+>")
    class Cosine(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<=>")
    class Inner(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<#>")
    class Hamming(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<~>")
    class Jaccard(expr1: Expression<*>, expr2: Expression<*>) : DistanceOps(expr1, expr2, "<%>")
}
