package org.vitrivr.engine.core.db

import org.vitrivr.engine.core.data.descriptor.Descriptor
import org.vitrivr.engine.core.data.query.Query

interface DescriptorReader<T : Descriptor> : PersistableReader<T> {

    fun getAll(query: Query<T>): Sequence<T>

}