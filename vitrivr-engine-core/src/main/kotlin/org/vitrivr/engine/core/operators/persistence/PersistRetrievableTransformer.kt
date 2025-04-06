package org.vitrivr.engine.core.operators.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
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

        /** The [RetrievableWriter] instance used by this [PersistRetrievableTransformer]. */
        private val reader: RetrievableReader by lazy {
            this.context.schema.connection.getRetrievableReader()
        }

        /**
         * Converts this [PersistRetrievableTransformer] to a [Flow]
         *
         * @param scope The [CoroutineScope] to use.
         * @return [Flow]
         */
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
            val cache = mutableMapOf<RetrievableId, MutableList<Relationship>>()
            return this.input.toFlow(scope).onEach {
                this.persist(it, cache)
            }.onCompletion {
                if (cache.isNotEmpty()) {
                    this@Instance.logger.warn { " ${cache.map { it.value.size }.sum()} relationships that could not be persisted due to missing retrievables." }
                }
            }
        }

        /**
         * This method persists [Retrievable]s and all associated [Descriptor]s and [Relationship]s.
         *
         * @param retrievable [Retrievable] to persist.
         * @param cache A [MutableMap] of [RetrievableId] to a list of [Relationship]s that are pending persistence.
         */
        private fun persist(retrievable: Retrievable, cache: MutableMap<RetrievableId, MutableList<Relationship>>) {
            /* Transient retrievable and retrievable that have already been persisted are ignored. */
            if (retrievable.transient) {
                this@Instance.logger.debug { "Skipped transient retrievable $retrievable for persistence." }
                return
            }

            /* Write retrievable to database. */
            this.writer.add(retrievable)

            /* If there are relationships that wait for retrievable to be persisted, persist them now. */
            val pendingRelationships = cache.remove(retrievable.id)
            if (pendingRelationships != null) {
                 this.writer.connectAll(pendingRelationships)
            }

            /* Now write relationships to database or cache them. */
            for (relationship in retrievable.relationships) {
                if (relationship.transient) continue

                if (relationship.subjectId == retrievable.id) {
                    if (this.reader.exists(relationship.objectId)) {
                        this.writer.connect(relationship)
                    } else {
                        cache.computeIfAbsent(relationship.objectId) { mutableListOf() }.add(relationship)
                    }
                } else if (relationship.objectId == retrievable.id) {
                    if (this.reader.exists(relationship.subjectId)) {
                        this.writer.connect(relationship)
                    } else {
                        cache.computeIfAbsent(relationship.subjectId) { mutableListOf() }.add(relationship)
                    }
                }
            }

            /* Write descriptors to database. */
            this@Instance.logger.trace { "Successfully retrievable $retrievable." }
        }
    }
}