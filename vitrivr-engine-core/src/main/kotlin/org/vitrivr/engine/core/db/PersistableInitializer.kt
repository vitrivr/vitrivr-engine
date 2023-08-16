package org.vitrivr.engine.core.db

import org.vitrivr.engine.core.data.Persistable

interface PersistableInitializer<out T : Persistable> {

    fun initialize()
    fun truncate()


}