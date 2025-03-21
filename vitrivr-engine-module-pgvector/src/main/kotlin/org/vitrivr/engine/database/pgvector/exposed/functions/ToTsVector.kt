package org.vitrivr.engine.database.pgvector.exposed.functions

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.TextColumnType

/**
 * [CustomFunction] to convert a [String] to a [tsvector](https://www.postgresql.org/docs/current/textsearch-controls.htm).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ToTsVector(config: Expression<String>?, document: Expression<String>) : CustomFunction<String>(
    "to_tsvector",
    TextColumnType(),
    *config?.let { arrayOf(config, document) } ?: arrayOf(document)
)

fun Expression<String>.toTsVector(config: Expression<String>? = null) = ToTsVector(config, this)