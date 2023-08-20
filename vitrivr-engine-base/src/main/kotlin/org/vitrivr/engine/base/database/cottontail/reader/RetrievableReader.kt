package org.vitrivr.engine.base.database.cottontail.reader

import io.grpc.StatusException
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import java.util.*

/**
 * A [RetrievableReader] implementation for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableReader(private val connection: CottontailConnection): RetrievableReader {
    /** The [Name.EntityName] for this [RetrievableInitializer]. */
    private val entityName: Name.EntityName = this.connection.schemaName.entity(CottontailConnection.RETRIEVABLE_ENTITY_NAME)

    override fun get(id: UUID): Retrievable? {
       TODO("Not yet implemented")
    }

    override fun getAll(ids: Iterable<UUID>): Sequence<Retrievable> {
        TODO("Not yet implemented")
    }

    override fun getAll(): Sequence<Retrievable> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }
}