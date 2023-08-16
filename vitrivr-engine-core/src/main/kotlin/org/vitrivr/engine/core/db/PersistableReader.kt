package org.vitrivr.engine.core.db

import org.vitrivr.engine.core.data.Persistable
import java.util.UUID

interface PersistableReader<out T : Persistable> {

    operator fun get(id: UUID): T?
    fun getAll(ids: Iterable<UUID>): Sequence<T>
    fun count(): Long

}