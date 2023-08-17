package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.database.Persistable

interface PersistableWriter<in T : Persistable> {

    fun add(item: T): Boolean
    fun addAll(items: Iterable<T>): Boolean
    fun update(item: T): Boolean
    fun delete(item: T): Boolean

}