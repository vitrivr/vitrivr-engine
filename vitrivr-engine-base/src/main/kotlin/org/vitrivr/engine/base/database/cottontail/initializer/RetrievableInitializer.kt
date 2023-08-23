package org.vitrivr.engine.base.database.cottontail.initializer

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.client.language.ddl.TruncateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ENTITY_NAME
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.database.retrievable.Retrievable

/** Defines [KLogger] of the class. */
private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [RetrievableInitializer] implementation for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableInitializer(private val connection: CottontailConnection): RetrievableInitializer {

    /** The [Name.EntityName] for this [RetrievableInitializer]. */
    private val entityName: Name.EntityName = Name.EntityName(this.connection.schemaName, RETRIEVABLE_ENTITY_NAME)

    /**
     * Initializes the entity that is used to store [Retrievable]s in Cottontail DB.
     */
    override fun initialize() {
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName("id"), Types.String, false, true, false)
        try {
            this.connection.client.create(create)
        } catch (e: StatusException) {
            logger.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }

    /**
     * Truncates the entity that is used to store [Retrievable]s in Cottontail DB.
     */
    override fun truncate() {
        val truncate = TruncateEntity(this.entityName)
        try {
            this.connection.client.truncate(truncate)
        } catch (e: StatusException) {
            logger.error(e) { "Failed to truncate entity ${this.entityName} due to exception." }
        }
    }
}