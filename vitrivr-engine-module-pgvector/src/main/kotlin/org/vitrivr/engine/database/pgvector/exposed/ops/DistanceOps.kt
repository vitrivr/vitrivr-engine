package org.vitrivr.engine.database.pgvector.exposed.ops

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap

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

/**
 * Performs a [DistanceOps.Euclidean] comparison operator on two [FloatArray]s.
 *
 * @param other The [FloatArray] to compare with.
 */
infix fun ExpressionWithColumnType<FloatArray>.euclidean(other: FloatArray) = DistanceOps.Euclidean(this, wrap(other))

/**
 * Performs a [DistanceOps.Euclidean] comparison operator on two [FloatArray]s.
 *
 * @param other The [FloatArray] to compare with.
 */
infix fun ExpressionWithColumnType<FloatArray>.manhattan(other: FloatArray) = DistanceOps.Manhattan(this, wrap(other))

/**
 * Performs a [DistanceOps.Cosine] comparison operator on two [FloatArray]s.
 *
 * @param other The [FloatArray] to compare with.
 */
infix fun ExpressionWithColumnType<FloatArray>.cosine(other: FloatArray) = DistanceOps.Cosine(this, wrap(other))

/**
 * Performs a [DistanceOps.Inner] comparison operator on two [FloatArray]s.
 *
 * @param other The [FloatArray] to compare with.
 */
infix fun ExpressionWithColumnType<FloatArray>.inner(other: FloatArray) = DistanceOps.Inner(this, wrap(other))