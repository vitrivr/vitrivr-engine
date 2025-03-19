package org.vitrivr.engine.database.pgvector.exposed.ops

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Expression

/**
 * PostgreSQL match operator for [String] values.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TsMatchOp(expr1: Expression<String>, expr2: Expression<String>) : ComparisonOp(expr1, expr2, "@@")

/**
 * Performs a [TsMatchOp] comparison operator on two [Expression]s.
 *
 * @param other The [Expression] to compare with.
 */
infix fun Expression<String>.tsMatches(other: Expression<String>) = TsMatchOp(this, other)