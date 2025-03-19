package org.vitrivr.engine.database.jsonl.retrievable

import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.jsonl.RELATIONSHIPS_FILE_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import org.vitrivr.engine.database.jsonl.RETRIEVABLES_FILE_NAME
import org.vitrivr.engine.database.jsonl.model.JsonlRelationship
import org.vitrivr.engine.database.jsonl.model.JsonlRetrievable
import java.nio.file.StandardOpenOption
import kotlin.io.path.writer

/**
 * A [RetrievableWriter] for the JSONL format. This class is responsible for writing [Retrievable]s to a JSONL file.
 *
 * @author Luca Rosetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
class JsonlRetrievableWriter(override val connection: JsonlConnection) : RetrievableWriter {
    /** Path to the file containing [Retrievable]s. */
    private val retrievablePath = this.connection.schemaRoot.resolve(RETRIEVABLES_FILE_NAME)

    /** Path to the file containing [Relationship]s. */
    private val connectionPath = this.connection.schemaRoot.resolve(RELATIONSHIPS_FILE_NAME)

    /**
     * Adds (and typically persists) a single [Retrievable] through this [JsonlRetrievableWriter].
     *
     * This method is synchronized to prevent concurrent writes to the file.
     *
     * @param item [Retrievable] to persist.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun add(item: Retrievable): Boolean {
        this.openRetrievableWriter().use { writer ->
            writer.write(Json.encodeToString(JsonlRetrievable(item)))
            writer.write("\n")
            writer.flush()
        }
        return true
    }

    /**
     * Adds (and typically persists) a batch of [Retrievable] through this [JsonlRetrievableWriter].
     *
     * This method is synchronized to prevent concurrent writes to the file.
     *
     * @param items [Iterable] of [Retrievable]  to persist.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun addAll(items: Iterable<Retrievable>): Boolean {
        this.openRetrievableWriter().use { writer ->
            items.forEach { item ->
                writer.write(Json.encodeToString(JsonlRetrievable(item)))
                writer.write("\n")
                writer.flush()
            }
        }
        return true
    }

    /**
     * Persists a [Relationship] through this [JsonlRetrievableWriter]
     *
     * This method is synchronized to prevent concurrent writes to the file.
     *
     * @param relationship [Relationship] to persist
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun connect(relationship: Relationship): Boolean {
        this.openConnectionWriter().use { writer ->
            writer.write(Json.encodeToString(JsonlRelationship(relationship)))
            writer.write("\n")
        }
        return true
    }

    /**
     * Persists a list of [Relationship]s through this [JsonlRetrievableWriter].
     *
     * This method is synchronized to prevent concurrent writes to the file.
     *
     * @param relationships An [Iterable] of [Relationship]s to persist.
     * @return True on success, false otherwise.
     */
    @Synchronized
    override fun connectAll(relationships: Iterable<Relationship>): Boolean {
        this.openConnectionWriter().use { writer ->
            relationships.forEach { relationship ->
                writer.write(Json.encodeToString(JsonlRelationship(relationship)))
                writer.write("\n")
            }
        }
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

    private fun openConnectionWriter() =
       this.connectionPath.writer(Charsets.UTF_8, StandardOpenOption.APPEND)

    private fun openRetrievableWriter() =
        this.retrievablePath.writer(Charsets.UTF_8, StandardOpenOption.APPEND)
}