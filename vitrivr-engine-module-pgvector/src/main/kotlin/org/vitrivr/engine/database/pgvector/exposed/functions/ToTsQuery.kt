package org.vitrivr.engine.database.pgvector.exposed.functions

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.TextColumnType
/**
 * [CustomFunction] to convert a [String] to a [tsquery](https://www.postgresql.org/docs/current/textsearch-controls.html).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ToTsQuery(config: Expression<String>?, query: Expression<String>) : CustomFunction<String>(
    "to_tsquery",
    TextColumnType(),
    *config?.let { arrayOf(config, query) } ?: arrayOf(query)
)

fun toTsQuery(query: Expression<String>, config: Expression<String>? = null) = ToTsQuery(config, query)