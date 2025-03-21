package org.vitrivr.engine.database.jsonl.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.jsonl.RELATIONSHIPS_FILE_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.LOGGER
import org.vitrivr.engine.database.jsonl.RETRIEVABLES_FILE_NAME
import java.io.IOException
import kotlin.io.path.*

class JsonlRetrievableInitializer(private val connection: JsonlConnection) : RetrievableInitializer {

    /** Path to the file containing [Retrievable]s. */
    private val retrievablePath = this.connection.schemaRoot.resolve(RETRIEVABLES_FILE_NAME)

    /** Path to the file containing [Relationship]s. */
    private val connectionPath = this.connection.schemaRoot.resolve(RELATIONSHIPS_FILE_NAME)

    override fun initialize() {
        try {
            this.connection.schemaRoot.createDirectories()
            this.retrievablePath.createFile()
            this.connectionPath.createFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot initialize '${connection.schemaRoot.absolutePathString()}'" }
        }
    }

    override fun deinitialize() {
        try {
            this.retrievablePath.deleteIfExists()
            this.connectionPath.deleteIfExists()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot delete '${connection.schemaRoot.absolutePathString()}'" }
        }
    }

    override fun isInitialized(): Boolean = retrievablePath.exists() && connectionPath.exists()

    override fun truncate() {
        try {
            this.retrievablePath.deleteIfExists()
            this.retrievablePath.createFile()
            this.connectionPath.deleteIfExists()
            this.connectionPath.createFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot truncate '${connection.schemaRoot.absolutePathString()}'" }
        }
    }
}