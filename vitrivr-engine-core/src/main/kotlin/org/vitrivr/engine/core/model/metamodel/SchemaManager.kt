package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.operators.ingest.ResolverFactory
import java.util.*
import org.vitrivr.engine.core.util.extension.loadServiceForName
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

    /** A [Map] of all available [ExporterFactory]s. These are loaded upon initialization of the class. */
    private val exporterFactories: Map<String, ExporterFactory> = HashMap()

    /** A [Map] of all available [ResolverFactory]s. These are loaded upon initialization of the class. */
    private val resolverFactories: Map<String, ResolverFactory> = HashMap()

    init {
        /* Reload analysers. */
        (this.analysers as MutableMap<String, Analyser<*, *>>).clear()
        for (a in ServiceLoader.load(Analyser::class.java)) {
            if (this.analysers.containsKey(a.analyserName)) {
                /* TODO: Log warning! */
            }
            this.analysers[a.analyserName] = a
        }

        /* Reload exporter factories. */
        (this.exporterFactories as MutableMap<String, ExporterFactory>).clear()
        for (e in ServiceLoader.load(ExporterFactory::class.java)) {
            if (this.exporterFactories.containsKey(e.name)) {
                /* TODO: Log warning! */
            }
            this.exporterFactories[e.name] = e
        }

        /* Reload resolver factories. */
        (this.resolverFactories as MutableMap<String, ResolverFactory>).clear()
        for (r in ServiceLoader.load(ResolverFactory::class.java)) {
            if (this.resolverFactories.containsKey(r.name)) {
                /* TODO: Log warning! */
            }
            this.resolverFactories[r.name] = r
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
        val connectionProvider = loadServiceForName<ConnectionProvider>(config.connection.database)
            ?: throw IllegalArgumentException("Failed to find connection provider implementation for '${config.connection.database}'.")

        /* Create new connection using reflection. */
        val connection = connectionProvider.openConnection(config.name, config.connection.parameters)
        val schema = Schema(config.name, connection)
        config.fields.map {
            @Suppress("UNCHECKED_CAST")
            schema.addField(it.name, this.getAnalyserForName(it.analyser) as Analyser<ContentElement<*>, Descriptor>, it.parameters)
        }
        config.exporters.map{
            @Suppress("UNCHECKED_CAST")
            schema.addExporter(it.name, this.getExporterFactoryForName(it.exporterFactory), it.exporterParameters, this.getResolverFactoryForName(it.resolverFactory), it.resolverParameters)
        }

        /* Cache and return connection. */
        this.schemas[schema.name] = schema
    }

    /**
     * Returns a [ResolverFactory] for the provided resolverFactory name.
     *
     * @param name [String]
     * @return [ResolverFactory] or null, if no [ResolverFactory] exists for given name.
     */
    fun getResolverFactoryForName(name: String): ResolverFactory = this.resolverFactories[name] ?: throw IllegalStateException("Failed to find resolver implementation for name '$name'.")

    /**
     * Returns an [ExporterFactory] for the provided exporterFactory name.
     *
     * @param name [String]
     * @return [ExporterFactory] or null, if no [ExporterFactory] exists for given name.
     */
    fun getExporterFactoryForName(name: String): ExporterFactory = this.exporterFactories[name] ?: throw IllegalStateException("Failed to find exporter implementation for name '$name'.")


    /**
     * Returns an [Analyser] for the provided analyser name.
     *
     * @param name [String]
     * @return [Analyser] or null, if no [Analyser] exists for given name.
     */
    fun getAnalyserForName(name: String): Analyser<*, *> = loadServiceForName<Analyser<*, *>>(name) ?: throw IllegalStateException("Failed to find analyser implementation for name '$name'.")

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