package org.vitrivr.engine.database.jsonl.retrievable

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import org.vitrivr.engine.database.jsonl.model.JsonlRelationship
import org.vitrivr.engine.database.jsonl.model.JsonlRetrievable
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.io.path.inputStream

class JsonlRetrievableReader(
    override val connection: JsonlConnection
) : RetrievableReader {

    private val retrievablePath =
        connection.schemaRoot.resolve(
            "retrievables.jsonl"
        )
    private val connectionPath =
        connection.schemaRoot.resolve(
            "retrievable_connections.jsonl"
        )

    override fun get(
        id: RetrievableId
    ): Retrievable? =
        getAll().firstOrNull { it.id == id }

    override fun exists(
        id: RetrievableId
    ): Boolean =
        get(id) != null

    override fun getAll(
        ids: Iterable<RetrievableId>
    ): Sequence<Retrievable> {
        val idSet =
            ids.toSet()
        return getAll().filter {
            idSet.contains(
                it.id
            )
        }
    }

    override fun getConnections(
        subjectIds: Collection<RetrievableId>,
        predicates: Collection<String>,
        objectIds: Collection<RetrievableId>
    ): Sequence<Triple<RetrievableId, String, RetrievableId>> {
        val subIds =
            subjectIds.toSet()
        val predIds =
            predicates.toSet()
        val objIds =
            objectIds.toSet()

        return BufferedReader(
            InputStreamReader(
                connectionPath.inputStream()
            )
        ).lineSequence()
            .mapNotNull {
                try {
                    Json.decodeFromString<JsonlRelationship>(
                        it
                    )
                } catch (se: SerializationException) {
                    LOGGER.error(
                        se
                    ) { "Error during deserialization" }
                    null
                } catch (ie: IllegalArgumentException) {
                    LOGGER.error(
                        ie
                    ) { "Error during deserialization" }
                    null
                }
            }
            .filter {
                (subIds.isEmpty() || subIds.contains(
                    it.sub
                )) &&
                        (predIds.isEmpty() || predIds.contains(
                            it.pred
                        )) &&
                        (objIds.isEmpty() || objIds.contains(
                            it.obj
                        ))
            }
            .map { it.toTriple() }
    }

    override fun getAll(): Sequence<Retrievable> {
        val inputstream =retrievablePath.inputStream()
        val br =BufferedReader(InputStreamReader(inputstream))
        val tmp = br.lineSequence().mapNotNull {
                    try {
                        Json.decodeFromString<JsonlRetrievable>(it).toRetrieved()
                    } catch (se: SerializationException) {
                        LOGGER.error(se) { "Error during deserialization" }
                        null
                    } catch (ie: IllegalArgumentException) {
                        LOGGER.error(ie) { "Error during deserialization" }
                        null
                    } catch (e: Exception) {
                        LOGGER.error(e) { "Error during deserialization" }
                        null
                    }
                }
        return tmp
    }


    override fun count(): Long =
        BufferedReader(InputStreamReader(retrievablePath.inputStream())).lineSequence().count().toLong()
}