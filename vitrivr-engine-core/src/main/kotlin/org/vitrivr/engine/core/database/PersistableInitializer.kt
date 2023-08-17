package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.database.Persistable

interface PersistableInitializer<out T : Persistable> {

    fun initialize()
    fun truncate()


}