package org.vitrivr.engine.database.jsonl.retrievable

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import org.vitrivr.engine.database.jsonl.model.JsonlRelationship
import org.vitrivr.engine.database.jsonl.model.JsonlRetrievable
import java.io.File
import java.io.FileWriter

class JsonlRetrievableWriter(override val connection: JsonlConnection) : RetrievableWriter, AutoCloseable {

    private val retrievableWriter = FileWriter(File(connection.schemaRoot, "retrievables.jsonl"), true)
    private val connectionWriter = FileWriter(File(connection.schemaRoot, "retrievable_connections.jsonl"), true)

    @Synchronized
    override fun connect(relationship: Relationship): Boolean {
        connectionWriter.write(Json.encodeToString(JsonlRelationship(relationship)))
        connectionWriter.write("\n")
        connectionWriter.flush()
        return true
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

    @Synchronized
    override fun add(item: Retrievable): Boolean {
        retrievableWriter.write(Json.encodeToString(JsonlRetrievable(item)))
        retrievableWriter.write("\n")
        retrievableWriter.flush()
        return true
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

    override fun close() {
        retrievableWriter.close()
        connectionWriter.close()
    }
}