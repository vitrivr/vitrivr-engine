package org.vitrivr.engine.index.database.util.initializer

import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.database.Persistable

open class NoInitializer<T : Persistable> : Initializer<T> {

    override fun initialize() {
        //nop
    }

    override fun truncate() {
        //nop
    }
}