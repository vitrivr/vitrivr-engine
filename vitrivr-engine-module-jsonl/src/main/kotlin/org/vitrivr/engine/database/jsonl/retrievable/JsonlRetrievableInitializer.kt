package org.vitrivr.engine.database.jsonl.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import java.io.IOException
import kotlin.io.path.*

class JsonlRetrievableInitializer(private val connection: JsonlConnection) : RetrievableInitializer {

    private val retrievablePath = connection.schemaRoot.resolve("retrievables.jsonl")
    private val connectionPath = connection.schemaRoot.resolve("retrievable_connections.jsonl")

    override fun initialize() {
        try {
            connection.schemaRoot.createDirectories()
            retrievablePath.createFile()
            connectionPath.createFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot initialize '${connection.schemaRoot.absolutePathString()}'" }
        }
    }

    override fun deinitialize() {
        try {
            retrievablePath.deleteExisting()
            connectionPath.deleteExisting()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot delete '${connection.schemaRoot.absolutePathString()}'" }
        }
    }

    override fun isInitialized(): Boolean = retrievablePath.exists() && connectionPath.exists()

    override fun truncate() {
        try {
            retrievablePath.deleteExisting()
            retrievablePath.createFile()
            connectionPath.deleteExisting()
            connectionPath.createFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot truncate '${connection.schemaRoot.absolutePathString()}'" }
        }
    }
}