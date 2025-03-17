package org.vitrivr.engine.database.pgvector

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 *
 */
fun <T> sequenceWithTx(db: Database? = null, block: suspend SequenceScope<T>.() -> Unit): Sequence<T> = Sequence {
    transaction(db) {
        iterator(block)
    }
}