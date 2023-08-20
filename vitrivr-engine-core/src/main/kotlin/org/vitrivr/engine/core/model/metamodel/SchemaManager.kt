package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * The central [Schema] manager used by vitrivr.
 *
 * The [SchemaManager] maps [Schema] definitions to database [Connection] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object SchemaManager {
    /** An internal [HashMap] of all open [Connection]. */
    private val connections = HashMap<String, Connection>()

    /** A [ReentrantReadWriteLock] to mediate concurrent access to this class. */
    private val lock = ReentrantReadWriteLock()

    /**
     * Opens a new [Connection] for the provided [Schema], using the provided [Map] of the connection parameters.
     *
     * @param schema The [Schema] to open a [Connection] for.
     * @param parameters A map of [Connection] parameters.
     */
    fun open(schema: Schema, parameters: Map<String,String> = emptyMap()): Connection = this.lock.write {
        /* Close existing connection. */
        if (this.connections.containsKey(schema.name)) {
            this.connections[schema.name]?.close()
        }

        /* Create new connection using reflection. */
        val type = schema.connection.connectionClass()
        val constructor = type.constructors.find { constructor ->
            constructor.parameters.size == 2 && constructor.parameters.any { it.name == "schema" } && constructor.parameters.any { it.name == "parameters" }
        } ?: throw IllegalArgumentException("Failed to find valid constructor for connection of type $type.")
        val instance = constructor.callBy(mapOf(
            constructor.parameters.first { it.name == "schema" } to schema,
            constructor.parameters.first { it.name == "parameters" } to parameters,
        ))

        /* Cache and return connection. */
        this.connections[schema.name] = instance
        return instance
    }

    /**
     * Lists all [Schema] managed by this [SchemaManager].
     *
     * @return [List] of [Schema]
     */
    fun listSchemas(): List<Schema> = this.lock.read { this.connections.values.map { it.schema } }

    /**
     * Returns a [Connection] for the provided [schemaName].
     *
     * @param schemaName The name of the [Schema] to return.
     * @return [Schema] or null
     */
    fun getSchema(schemaName: String): Schema? = this.lock.read { this.connections[schemaName]?.schema }

    /**
     * Returns a [Connection] for the provided [Schema].
     *
     * @param schema The [Schema] to return [Connection] for.
     * @return [Connection] or null
     */
    fun getConnection(schema: Schema): Connection? = getConnection(schema.name)

    /**
     * Returns a [Connection] for the provided [schemaName].
     *
     * @param [schemaName] The name of the [Schema] to return [Connection] for.
     * @return [Connection] or null
     */
    fun getConnection(schemaName: String): Connection? = this.lock.read {
        this.connections[schemaName]
    }

    /**
     * Closes a [Schema] with the provided [schemaName].
     *
     * @param [schema] The name of the [Schema] to close.
     * @return True on success, false otherwise.
     */
    fun close(schema: Schema): Boolean = close(schema.name)

    /**
     * Closes a [Schema] with the provided [schemaName].
     *
     * @param [schemaName] The name of the [Schema] to close.
     */
    fun close(schemaName: String): Boolean = this.lock.write {
        if (this.connections.containsKey(schemaName)) {
            this.connections[schemaName]?.close()
            this.connections.remove(schemaName)
            true
        } else {
            false
        }
    }
}