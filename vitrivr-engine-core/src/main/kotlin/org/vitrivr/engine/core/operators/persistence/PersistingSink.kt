package org.vitrivr.engine.core.operators.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Sink] that persists the [Ingested] it receives.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PersistingSink(override val input: Operator<Retrievable>, val context: IndexContext) : Operator.Sink<Retrievable> {

    /** Logger instance. */
    private val logger = KotlinLogging.logger {}

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
        this@PersistingSink.input.toFlow(scope).collect { persist(it) }
        emit(Unit)
    }

    /**
     * This method persists [Retrievable]s and all associated [Descriptor]s and [Relationship]s.
     *
     * @param retrievable [Retrievable] to persist.
     */
    @Suppress("UNCHECKED_CAST")
    private fun persist(retrievable: Retrievable) {
        /* Transient retrievable and retrievable that have already been persisted are ignored. */
        if (retrievable.transient) return

        /* Collection all entites that should be persisted. */
        val retrievables = mutableSetOf<Retrievable>()
        val relationships = mutableSetOf<Relationship>()
        val descriptors = mutableMapOf<Schema.Field<*, *>, MutableSet<Descriptor>>()
        collect(retrievable, Triple(retrievables, relationships, descriptors))

        /* Write entities to database. */
        this.writer.connection.withTransaction {
            this.writer.addAll(retrievables)
            this.writer.connectAll(relationships)
            for ((f, d) in descriptors) {
                val writer = f.let { field -> this.descriptorWriters.computeIfAbsent(field) { it.getWriter() } } as? DescriptorWriter<Descriptor>
                if (writer?.addAll(d) != true) {
                    logger.error { "Failed to persist descriptors for field ${f.fieldName}." }
                }
            }
        }
    }

    /**
     * Collects all [Retrievable]s, [Relationship]s and [Descriptor]s that are reachable from the given [Retrievable] and should be persisted.s
     */
    private fun collect(retrievable: Retrievable, into: Triple<MutableSet<Retrievable>, MutableSet<Relationship>, MutableMap<Schema.Field<*, *>, MutableSet<Descriptor>>>) {
        if (retrievable.transient) return

        /* Add retrievable. */
        into.first.add(retrievable)

        /* Add relationships. */
        for (relationship in retrievable.relationships) {
            if (!relationship.transient) {
                into.second.add(relationship)
                if (relationship.subjectId == retrievable.id && relationship is Relationship.WithObject && !into.first.contains(relationship.`object`)) {
                    collect(relationship.`object`, into)
                } else if (relationship.objectId == retrievable.id && relationship is Relationship.WithSubject && !into.first.contains(relationship.subject)) {
                    collect(relationship.subject, into)
                }
            }
        }

        /* Add descriptors. */
        for (descriptor in retrievable.descriptors) {
            val field = descriptor.field
            if (field != null) {
                into.third.compute(field) { _, v -> (v ?: mutableSetOf()).apply { add(descriptor) } }
            }else{
                logger.debug { "Descriptor $descriptor has no field and will not be persisted." }
            }
        }
    }
}