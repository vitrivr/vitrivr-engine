package org.vitrivr.engine.database.jsonl.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import java.io.File
import java.io.IOException

class JsonlRetrievableInitializer(private val connection: JsonlConnection) : RetrievableInitializer {

    private val retrievableFile = File(connection.schemaRoot, "retrievables.jsonl")
    private val connectionFile = File(connection.schemaRoot, "retrievable_connections.jsonl")

    override fun initialize() {
        try {
            connection.schemaRoot.mkdirs()
            retrievableFile.createNewFile()
            connectionFile.createNewFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot initialize '${connection.schemaRoot.absolutePath}'" }
        } catch (se: SecurityException) {
            LOGGER.error(se) { "Cannot initialize '${connection.schemaRoot.absolutePath}'" }
        }
    }

    override fun deinitialize() {
        /* nop */
    }

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }

    override fun truncate() {
        try {
            retrievableFile.delete()
            connectionFile.delete()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot delete '${connection.schemaRoot.absolutePath}'" }
        } catch (se: SecurityException) {
            LOGGER.error(se) { "Cannot delete '${connection.schemaRoot.absolutePath}'" }
        }
    }
}