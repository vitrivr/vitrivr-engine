package org.vitrivr.engine.database.pgvector.exposed

import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap
import org.vitrivr.engine.database.pgvector.exposed.ops.DistanceOps
import org.vitrivr.engine.database.pgvector.exposed.ops.FulltextOp

/**
 *
 */
infix fun ExpressionWithColumnType<String>.fulltext(other: String): Op<Boolean> = FulltextOp(this, wrap(other))

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
