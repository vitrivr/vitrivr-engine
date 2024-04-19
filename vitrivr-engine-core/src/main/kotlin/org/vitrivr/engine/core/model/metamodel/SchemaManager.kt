package org.vitrivr.engine.core.model.metamodel

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.vitrivr.engine.core.config.IndexConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.resolver.ResolverFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.system.exitProcess

private val logger: KLogger = KotlinLogging.logger {}

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

    /**
     * Loads the [SchemaConfig] with this [SchemaManager].
     *
     * @param config The [SchemaConfig] to load.
     */
    @OptIn(ExperimentalSerializationApi::class)
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
            val analyser = loadServiceForName<Analyser<*,*>>(it.factory) ?: throw IllegalArgumentException("Failed to find a factory implementation for '${it.factory}'.")
            if(it.name.contains(".")){
                throw IllegalArgumentException("Field names must not have a dot (.) in their name.")
            }
            @Suppress("UNCHECKED_CAST")
            schema.addField(it.name, analyser as Analyser<ContentElement<*>, Descriptor>, it.parameters)
        }
        config.exporters.map {
            schema.addExporter(
                it.name,
                loadServiceForName<ExporterFactory>(it.factory) ?: throw IllegalArgumentException("Failed to find exporter factory implementation for '${it.factory}'."),
                it.parameters,
                (loadServiceForName<ResolverFactory>(it.resolver.factory) ?: throw IllegalArgumentException("Failed to find resolver factory implementation for '${it.resolver.factory}'.")).newResolver(schema, it.resolver.parameters),
            )
        }
        config.extractionPipelines.map {
            val indexConfig = IndexConfig.read(Paths.get(it.path))
                ?: throw IllegalArgumentException("Failed to read pipeline configuration from '${it.path}'.")
            if (indexConfig.schema != schema.name) {
                throw IllegalArgumentException("Schema name in pipeline configuration '${indexConfig.schema}' does not match schema name '${schema.name}'.")
            }
            schema.addPipeline(it.name, indexConfig)
        }

        /* Cache and return connection. */
        this.schemas[schema.name] = schema
    }

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
