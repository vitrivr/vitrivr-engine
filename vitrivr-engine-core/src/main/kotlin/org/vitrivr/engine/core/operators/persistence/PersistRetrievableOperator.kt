package org.vitrivr.engine.core.operators.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Sink] that persists the [Ingested] it receives.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
class PersistRetrievableOperator(
    override val input: Operator<Retrievable>,
    val context: IndexContext,
    override val name: String
) : Operator.Unary<Retrievable, Retrievable> {

    /** Logger instance. */
    private val logger = KotlinLogging.logger {}

    /** The [RetrievableWriter] instance used by this [PersistRetrievableOperator]. */
    private val writer: RetrievableWriter by lazy {
        this.context.schema.connection.getRetrievableWriter()
    }

    /**
     * Converts this [PersistRetrievableOperator] to a [Flow]
     *
     * @param scope The [CoroutineScope] to use.
     * @return [Flow]
     */
    override fun toFlow(scope: CoroutineScope) = this@PersistRetrievableOperator.input.toFlow(scope).onEach { retrievable ->
        if (!retrievable.transient) {
            persist(retrievable)
        }
    }

    /**
     * This method persists [Retrievable]s and all associated [Descriptor]s and [Relationship]s.
     *
     * @param retrievable [Retrievable] to persist.
     */
    private fun persist(retrievable: Retrievable) {
        /* Write entities to database. */
        this.writer.connection.withTransaction {
            this.writer.add(retrievable)
            this.writer.connectAll(retrievable.relationships)
        }
        logger.debug { "Persisted retrievable ${retrievable.id}, with ${retrievable.relationships.size} relationships." }
    }
}