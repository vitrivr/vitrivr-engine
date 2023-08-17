package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.query.Query

interface DescriptorReader<T : Descriptor> : PersistableReader<T> {

    fun getAll(query: Query<T>): Sequence<T>

}