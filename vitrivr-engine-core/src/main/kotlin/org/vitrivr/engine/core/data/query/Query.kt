package org.vitrivr.engine.core.data.query

import org.vitrivr.engine.core.data.descriptor.Descriptor

interface Query<T : Descriptor> {

    val descriptor: T

}