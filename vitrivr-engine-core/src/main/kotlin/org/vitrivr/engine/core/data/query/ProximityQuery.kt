package org.vitrivr.engine.core.data.query

import org.vitrivr.engine.core.data.descriptor.VectorDescriptor

interface ProximityQuery<T : VectorDescriptor> : Query<T> {

    val numberOfVectors: Int
    val order: Order



    enum class Order {
        ASC,
        DESC
    }

}