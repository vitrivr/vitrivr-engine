package org.vitrivr.engine.database.jsonl.retrievable

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import org.vitrivr.engine.database.jsonl.RELATIONSHIPS_FILE_NAME
import org.vitrivr.engine.database.jsonl.RETRIEVABLES_FILE_NAME
import org.vitrivr.engine.database.jsonl.model.JsonlRelationship
import org.vitrivr.engine.database.jsonl.model.JsonlRetrievable
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.io.path.inputStream

class JsonlRetrievableReader(override val connection: JsonlConnection) : RetrievableReader {

    /** Path to the file containing [Retrievable]s. */
    private val retrievablePath = this.connection.schemaRoot.resolve(RETRIEVABLES_FILE_NAME)

    /** Path to the file containing [Relationship]s. */
    private val connectionPath = this.connection.schemaRoot.resolve(RELATIONSHIPS_FILE_NAME)

    override fun get(id: RetrievableId): Retrieved? = getAll().firstOrNull { it.id == id }

    override fun exists(id: RetrievableId): Boolean = get(id) != null

    override fun getAll(ids: Iterable<RetrievableId>): Sequence<Retrieved> {
        val idSet = ids.toSet()
        return getAll().filter { idSet.contains(it.id) }
    }

    override fun getConnections(
        subjectIds: Collection<RetrievableId>,
        predicates: Collection<String>,
        objectIds: Collection<RetrievableId>
    ): Sequence<Relationship.ById> {
        val subIds = subjectIds.toSet()
        val predIds = predicates.toSet()
        val objIds = objectIds.toSet()

        return BufferedReader(InputStreamReader(connectionPath.inputStream())).lineSequence().mapNotNull {
            try {
                Json.decodeFromString<JsonlRelationship>(it)
            } catch (se: SerializationException) {
                LOGGER.error(se) { "Error during deserialization" }
                null
            } catch (ie: IllegalArgumentException) {
                LOGGER.error(ie) { "Error during deserialization" }
                null
            }
        }.filter {
            (subIds.isEmpty() || subIds.contains(it.sub)) &&
                    (predIds.isEmpty() || predIds.contains(it.pred)) &&
                    (objIds.isEmpty() || objIds.contains(it.obj))
        }.map { it.toRelationship() }
    }

    override fun getAll(): Sequence<Retrieved> {
        return BufferedReader(InputStreamReader(this.retrievablePath.inputStream())).lineSequence().mapNotNull {
            try {
                Json.decodeFromString<JsonlRetrievable>(it).toRetrieved()
            } catch (se: SerializationException) {
                LOGGER.error(se) { "Error during deserialization" }
                null
            } catch (ie: IllegalArgumentException) {
                LOGGER.error(ie) { "Error during deserialization" }
                null
            }
        }
    }

    override fun count(): Long =
        BufferedReader(InputStreamReader(this.retrievablePath.inputStream())).lineSequence().count().toLong()
}