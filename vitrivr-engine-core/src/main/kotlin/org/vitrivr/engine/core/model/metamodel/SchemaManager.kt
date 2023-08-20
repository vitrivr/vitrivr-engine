package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import java.util.ServiceLoader
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

    /** A [Map] of all available [Analyser]s. */
    private val analysers = HashMap<String,Analyser<*>>()

    init {
        for (a in ServiceLoader.load(Analyser::class.java)) {
            if (this.analysers.containsKey(a.analyserName)) {
                /* TODO: Log warning! */
            }
            this.analysers[a.analyserName] = a
        }
    }

    /**
     * Returns an [Analyser] for the provided analyser name.
     *
     * @param name [String]
     * @return [Analyser] or null, if no [Analyser] exists for given name.
     */
    fun getAnalyserForName(name: String): Analyser<*> = (this.analysers[name]) ?: throw IllegalStateException("Failed to find analyser implementation for name '$name'.")

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

        /* Find connection provider for connection. */
        val provider = ServiceLoader.load(ConnectionProvider::class.java).find {
            it.databaseName == schema.connection.databaseName
        } ?: throw IllegalArgumentException("Failed to find connection provider implementation for '${schema.connection.databaseName}'.")

        /* Create new connection using reflection. */
        val connection = provider.openConnection(schema, parameters)

        /* Cache and return connection. */
        this.connections[schema.name] = connection
        return connection
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