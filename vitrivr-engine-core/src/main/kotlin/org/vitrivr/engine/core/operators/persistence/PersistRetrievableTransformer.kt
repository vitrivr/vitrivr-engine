package org.vitrivr.engine.core.operators.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Operator.Sink] that persists the [Ingested] it receives.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
class PersistRetrievableTransformer: TransformerFactory {
    /**
     * Creates and returns a new [PersistRetrievableTransformer.Instance] from this [PersistRetrievableTransformer].
     *
     * @param name The name of this [PersistRetrievableTransformer].
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     */
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer = Instance(
        input as Operator<Retrievable>,
        context as IndexContext,
        name
    )

    /**
     * The [PersistRetrievableTransformer] [Transformer] implementation.
     */
    private class Instance(override val input: Operator<Retrievable>, val context: IndexContext, override val name: String): Transformer {

        /** Logger instance. */
        private val logger = KotlinLogging.logger("RetrievablePersister#${this.name}")

        /** The [RetrievableWriter] instance used by this [PersistRetrievableTransformer]. */
        private val writer: RetrievableWriter by lazy {
            this.context.schema.connection.getRetrievableWriter()
        }

        /**
         * Converts this [PersistRetrievableTransformer] to a [Flow]
         *
         * @param scope The [CoroutineScope] to use.
         * @return [Flow]
         */
        override fun toFlow(scope: CoroutineScope) = this.input.toFlow(scope).onEach {
            this.persist(it)
        }

        /**
         * This method persists [Retrievable]s and all associated [Descriptor]s and [Relationship]s.
         *
         * @param retrievable [Retrievable] to persist.
         */
        private fun persist(retrievable: Retrievable) {
            /* Transient retrievable and retrievable that have already been persisted are ignored. */
            if (retrievable.transient) {
                this@Instance.logger.debug { "Skipped transient retrievable $retrievable for persistence." }
                return
            }

            /* Write retrievable to database. */
            this.writer.add(retrievable)

            /* Write relationships to database. */
            for (r in retrievable.relationships) {
                if (r.transient) continue
                this.writer.connect(r)
            }

            /* Write descriptors to database. */
            this@Instance.logger.trace { "Successfully retrievable $retrievable." }
        }
    }
}