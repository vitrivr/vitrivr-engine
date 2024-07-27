package org.vitrivr.engine.database.jsonl

import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable

class JsonlRetrievableWriter(override val connection: JsonlConnection) : RetrievableWriter {

    override fun connect(relationship: Relationship): Boolean {
        TODO("Not yet implemented")
    }

    override fun connectAll(relationships: Iterable<Relationship>): Boolean {
        relationships.forEach { connect(it) }
        return true
    }

    override fun disconnect(relationship: Relationship): Boolean {
        LOGGER.warn { "JsonlRetrievableWriter.disconnect is not supported" }
        return false
    }

    override fun disconnectAll(relationships: Iterable<Relationship>): Boolean {
        LOGGER.warn { "JsonlRetrievableWriter.disconnectAll is not supported" }
        return false
    }

    override fun add(item: Retrievable): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(items: Iterable<Retrievable>): Boolean {
        items.forEach { add(it) }
        return true
    }

    override fun update(item: Retrievable): Boolean {
        LOGGER.warn { "JsonlRetrievableWriter.update is not supported" }
        return false
    }

    override fun delete(item: Retrievable): Boolean {
        LOGGER.warn { "JsonlRetrievableWriter.delete is not supported" }
        return false
    }

    override fun deleteAll(items: Iterable<Retrievable>): Boolean {
        LOGGER.warn { "JsonlRetrievableWriter.deleteAll is not supported" }
        return false
    }

    fun close() {

    }
}