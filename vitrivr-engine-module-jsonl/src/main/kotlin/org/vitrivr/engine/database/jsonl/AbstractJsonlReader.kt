package org.vitrivr.engine.database.jsonl

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.io.path.inputStream

abstract class AbstractJsonlReader<D : Descriptor<*>>(
    final override val field: Schema.Field<*, D>,
    final override val connection: JsonlConnection
) :
    DescriptorReader<D> {

    /** Prototype used to create new instances. */
    protected val prototype = this.field.analyser.prototype(this.field)

    private val path = connection.getPath(field)

    protected abstract fun toDescriptor(list: AttributeContainerList): D

    override fun exists(descriptorId: DescriptorId): Boolean {
        return getAll().any { it.id == descriptorId }
    }

    override fun get(descriptorId: DescriptorId): D? {
        return getAll().firstOrNull { it.id == descriptorId }
    }

    override fun getAll(descriptorIds: Iterable<DescriptorId>): Sequence<D> {
        val ids = descriptorIds.toSet()
        return getAll().filter { ids.contains(it.id) }
    }

    override fun getAll(): Sequence<D> {

        return BufferedReader(InputStreamReader(path.inputStream())).lineSequence().mapNotNull {
            try {
                val list = Json.decodeFromString<AttributeContainerList>(it)
                return@mapNotNull toDescriptor(list)
            } catch (se: SerializationException) {
                LOGGER.error(se) { "Error during deserialization" }
                null
            } catch (ie: IllegalArgumentException) {
                LOGGER.error(ie) { "Error during deserialization" }
                null
            }
        }

    }

    override fun queryAndJoin(query: Query): Sequence<Retrieved> {
        val results = query(query).toList()
        val ids = results.mapNotNull { it.retrievableId }

        val retrievables = connection.getRetrievableReader().getAll(ids).associateBy { it.id }

        return results.map { descriptor ->
            val retrieved = retrievables[descriptor.retrievableId]!!
            retrieved.addDescriptor(descriptor)
            retrieved as Retrieved
        }.asSequence()
    }

    override fun getForRetrievable(retrievableId: RetrievableId): Sequence<D> {
        return getAll().filter { it.retrievableId == retrievableId}
    }

    override fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>): Sequence<D> {
        val ids = retrievableIds.toSet()
        return getAll().filter { ids.contains(it.retrievableId) }
    }


    override fun count(): Long {
        return BufferedReader(InputStreamReader(path.inputStream())).lineSequence().count().toLong()
    }
}