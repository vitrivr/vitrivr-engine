package org.vitrivr.engine.core.operators.persistence

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Sink] that persists the [Ingested] it receives.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PersistingSink(override val input: Operator<Ingested>, val context: IndexContext): Operator.Sink<Ingested> {

    /** The [RetrievableWriter] instance used by this [PersistingSink]. */
    private val writer: RetrievableWriter by lazy {
        this.context.schema.connection.getRetrievableWriter()
    }

    /** A [HashMap] of cached [DescriptorWriter] instances. */
    private val descriptorWriters = HashMap<Schema.Field<*,*>,DescriptorWriter<out Descriptor>>()

    /**
     * Converts this [PersistingSink] to a [Flow]
     *
     * @param scope The [CoroutineScope] to use.
     * @return [Flow]
     */
    override fun toFlow(scope: CoroutineScope): Flow<Unit> = flow {
        val persisted = mutableSetOf<RetrievableId>() /* Set of retrievable Ids that have been persisted. */
        this@PersistingSink.input.toFlow(scope).collect {
            persist(it, persisted)
        }
        emit(Unit)
    }

    /**
     * This method persists [Retrievable]s and all associated [DescriptorAttribute]s and [Relationship]s.
     *
     * @param retrievable [Retrievable] to persist.
     * @param persisted A [MutableSet] of all [Retrievable]
     */
    private fun persist(retrievable: Retrievable, persisted: MutableSet<RetrievableId>) {
        /* Transient retrievable and retrievable that have already been persisted are ignored. */
        if (retrievable.transient || retrievable.id in persisted) return

        /* Persist retrievable and add it to list of persisted retrievable. */
        if (!this.writer.add(retrievable)) return
        persisted.add(retrievable.id)

        /* Persist descriptors. */
        for (descriptor in retrievable.descriptors) {
            val writer = descriptor.field?.let { field -> this.descriptorWriters.computeIfAbsent(field) { it.getWriter() } } as? DescriptorWriter<Descriptor>
            writer?.add(descriptor)
        }

        /* Persist relationships. */
        for (relationship in retrievable.relationships) {
            if (!relationship.transient) {
                if (relationship is Relationship.ByRef) {
                    this.persist(relationship.subject, persisted)
                    this.persist(relationship.`object`, persisted)
                }
                this.writer.connect(relationship.subjectId, relationship.predicate, relationship.objectId)
            }
        }
    }
}