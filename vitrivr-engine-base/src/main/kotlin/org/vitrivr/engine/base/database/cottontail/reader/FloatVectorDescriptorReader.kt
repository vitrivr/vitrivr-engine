package org.vitrivr.engine.base.database.cottontail.reader

import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Field

/**
 * An [AbstractDescriptorReader] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class FloatVectorDescriptorReader(field: Field<FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorReader<FloatVectorDescriptor>(field, connection) {
    override fun getAll(query: Query<FloatVectorDescriptor>): Sequence<FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }

    override fun tupleToDescriptor(tuple: Tuple): FloatVectorDescriptor {
        TODO("Not yet implemented")
    }
}