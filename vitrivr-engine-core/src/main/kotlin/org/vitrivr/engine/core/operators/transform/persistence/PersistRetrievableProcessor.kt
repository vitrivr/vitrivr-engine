package org.vitrivr.engine.core.operators.transform.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Processor
import org.vitrivr.engine.core.operators.general.ProcessorFactory

/**
 * A [Operator.Sink] that persists the [Retrievable]s it receives.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PersistRetrievableProcessor: ProcessorFactory {

    /**
     * Generates and returns a new [PersistRetrievableProcessor].
     *
     * @param name Name of the [PersistRetrievableProcessor]
     * @param input [Operator] that acts as input.
     * @param context [Context] used by the [PersistRetrievableProcessor].
     */
    override fun newProcessor(name: String, input: Operator<Retrievable>, context: Context): Processor = Instance(
        name, input, context
    )

    /**
     * [Processor] that persists retrievable marked as non-transient.
     */
    private class Instance(name: String, input: Operator<Retrievable>, val context: Context) : Processor(name, input) {

        /** Logger instance. */
        private val logger = KotlinLogging.logger {}

        /** The [RetrievableWriter] instance used by this [PersistRetrievableProcessor]. */
        private val writer: RetrievableWriter by lazy {
            this.context.schema.connection.getRetrievableWriter()
        }

        /**
         * This method persists [Retrievable]s and all associated [Descriptor]s and [Relationship]s.
         *
         * @param retrievable [Retrievable] to persist.
         */
        override fun process(retrievable: Retrievable) {
            if (!retrievable.transient) {
                this.writer.add(retrievable)
                this.writer.connectAll(retrievable.relationships)
                logger.debug { "Persisted retrievable ${retrievable.id}, with ${retrievable.relationships.size} relationships." }
            }
        }
    }
}