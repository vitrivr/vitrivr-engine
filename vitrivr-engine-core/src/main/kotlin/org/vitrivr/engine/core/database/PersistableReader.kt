package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.database.Persistable
import java.util.UUID

interface PersistableReader<out T : Persistable> {

    operator fun get(id: UUID): T?
    fun getAll(ids: Iterable<UUID>): Sequence<T>
    fun count(): Long

}