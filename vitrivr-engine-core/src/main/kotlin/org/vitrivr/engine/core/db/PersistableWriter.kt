package org.vitrivr.engine.core.db

import org.vitrivr.engine.core.data.Persistable

interface PersistableWriter<in T : Persistable> {

    fun add(item: T): Boolean
    fun addAll(items: Iterable<T>): Boolean
    fun update(item: T): Boolean
    fun delete(item: T): Boolean

}