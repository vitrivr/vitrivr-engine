package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import java.util.*
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
class SchemaManager {
    /** An internal [HashMap] of all open [Connection]. */
    private val schemas = HashMap<String, Schema>()

    /** A [ReentrantReadWriteLock] to mediate concurrent access to this class. */
    private val lock = ReentrantReadWriteLock()

    /** A [Map] of all available [Analyser]s. These are loaded upon initialization of the class. */
    private val analysers: Map<String, Analyser<*, *>> = HashMap()

    init {
        /* Reload analysers. */
        (this.analysers as MutableMap<String, Analyser<*, *>>).clear()
        for (a in ServiceLoader.load(Analyser::class.java)) {
            if (this.analysers.containsKey(a.analyserName)) {
                /* TODO: Log warning! */
            }
            this.analysers[a.analyserName] = a
        }
    }

    /**
     * Loads the [SchemaConfig] with this [SchemaManager].
     *
     * @param config The [SchemaConfig] to load.
     */
    fun load(config: SchemaConfig) {
        /* Close existing connection (if exists). */
        if (this.schemas.containsKey(config.name)) {
            this.schemas[config.name]?.close()
        }

        /* Find connection provider for connection. */
        val connectionProvider = ServiceLoader.load(ConnectionProvider::class.java).find {
            it.databaseName == config.connection.database
        } ?: throw IllegalArgumentException("Failed to find connection provider implementation for '${config.connection.database}'.")

        /* Create new connection using reflection. */
        val connection = connectionProvider.openConnection(config.name, config.connection.parameters)
        val schema = Schema(config.name, connection)
        config.fields.map {
            @Suppress("UNCHECKED_CAST")
            schema.addField(it.name, this.getAnalyserForName(it.analyser) as Analyser<ContentElement<*>, Descriptor>, it.parameters)
        }

        /* Cache and return connection. */
        this.schemas[schema.name] = schema
    }


    /**
     * Returns an [Analyser] for the provided analyser name.
     *
     * @param name [String]
     * @return [Analyser] or null, if no [Analyser] exists for given name.
     */
    fun getAnalyserForName(name: String): Analyser<*, *> = this.analysers[name] ?: throw IllegalStateException("Failed to find analyser implementation for name '$name'.")

    /**
     * Lists all [Schema] managed by this [SchemaManager].
     *
     * @return [List] of [Schema]
     */
    fun listSchemas(): List<Schema> = this.lock.read { this.schemas.values.map { it } }

    /**
     * Returns a [Connection] for the provided [schemaName].
     *
     * @param schemaName The name of the [Schema] to return.
     * @return [Schema] or null
     */
    fun getSchema(schemaName: String): Schema? = this.lock.read { this.schemas[schemaName] }

    /**
     * Closes a [Schema].
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
        if (this.schemas.containsKey(schemaName)) {
            this.schemas[schemaName]?.close()
            this.schemas.remove(schemaName)
            true
        } else {
            false
        }
    }
}