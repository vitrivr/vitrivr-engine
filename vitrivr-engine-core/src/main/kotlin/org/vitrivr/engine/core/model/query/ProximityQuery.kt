package org.vitrivr.engine.core.model.query

import org.vitrivr.engine.core.model.database.descriptor.vector.VectorDescriptor


interface ProximityQuery<T : VectorDescriptor<*>> : Query<T> {

    val numberOfVectors: Int
    val order: Order



    enum class Order {
        ASC,
        DESC
    }

}