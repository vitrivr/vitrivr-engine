package org.vitrivr.engine.database.pgvector.exposed.index

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DdlAware
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.database.pgvector.exposed.types.VectorColumnType
import org.vitrivr.engine.database.pgvector.toIndexName

/**
 * A [VectorIndex] is a special type of index that is used to create a vector index on a [Column].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class VectorIndex(val column: Column<*>, val type: String, val distance: Distance, val customName: String? = null, val parameters: Map<String,String> = emptyMap()): DdlAware {

    companion object {
        val INDEXES_NNS = setOf("hnsw", "ivfflat")
    }

    init {
        require(column.columnType is VectorColumnType) {
            "Column ${column.name} is not a vector column. Vector index can therefore not be created."
        }
        require(INDEXES_NNS.contains(this.type.lowercase())) {
            "Unsupported index type ${this.type.lowercase()}."
        }
    }

    /**
     * Returns the create statement for this [VectorIndex].
     *
     * @return [List] of SQL statements.
     */
    override fun createStatement(): List<String> {
        val baseStatement = "CREATE INDEX ${this.customName ?: "${this.type.lowercase()}_${this.column.nameInDatabaseCase()}"} ON ${this.column.table.nameInDatabaseCase()} USING ${this.type.lowercase()}(${this.column.nameInDatabaseCase()} ${distance.toIndexName()})"
        if (parameters.isNotEmpty()) {
            val hyperparameters = parameters.entries.joinToString(", ") { "${it.key} = ${it.value}" }
            return listOf("$baseStatement WITH ($hyperparameters);")
        } else {
            return listOf("$baseStatement;")
        }
    }

    /**
     * Returns the create statement for this [VectorIndex].
     *
     * @return [List] of SQL statements.
     */
    override fun dropStatement(): List<String> = listOf(
        "DROP INDEX ${this.customName ?: "${this.type.lowercase()}_${this.column.nameInDatabaseCase()}"};"
    )

    /**
     * Returns the modify statement for this [VectorIndex].
     *
     * @return [List] of SQL statements.
     */
    override fun modifyStatement(): List<String> = this.dropStatement() + this.createStatement()
}