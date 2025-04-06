package org.vitrivr.engine.core.model.metamodel

import kotlinx.serialization.ExperimentalSerializationApi
import org.vitrivr.engine.core.config.ingest.IngestionConfig
import org.vitrivr.engine.core.config.schema.SchemaConfig
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.operators.general.ExporterFactory
import org.vitrivr.engine.core.resolver.ResolverFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import java.nio.file.Paths
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * The central [Schema] manager used by vitrivr.
 *
 * The [SchemaManager] maps [Schema] definitions to database [Connection] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.1
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
        config.fields.forEach { (fieldName, fieldConfig) ->
            val analyser = loadServiceForName<Analyser<*,*>>(fieldConfig.factory) ?: throw IllegalArgumentException("Failed to find a factory implementation for '${fieldConfig.factory}'.")
            if(fieldName.contains(".")){
                throw IllegalArgumentException("Field names must not have a dot (.) in their name.")
            }
            @Suppress("UNCHECKED_CAST")
            schema.addField(fieldName, analyser as Analyser<ContentElement<*>, Descriptor<*>>, fieldConfig.parameters, fieldConfig.indexes)
        }
        config.resolvers.forEach { (resolverName, resolverConfig) ->
            schema.addResolver(resolverName, (loadServiceForName<ResolverFactory>(resolverConfig.factory) ?: throw IllegalArgumentException("Failed to find resolver factory implementation for '${resolverConfig.factory}'.")).newResolver(schema, resolverConfig.parameters))
        }
        config.exporters.forEach { (exporterName, exporterConfig) ->
            schema.addExporter(
                exporterName,
                loadServiceForName<ExporterFactory>(exporterConfig.factory) ?: throw IllegalArgumentException("Failed to find exporter factory implementation for '${exporterConfig.factory}'."),
                exporterConfig.parameters
            )
        }
        config.extractionPipelines.forEach { (extractionPipelineName, extractionPipelineConfig) ->
            val ingestionConfig = IngestionConfig.read(Paths.get(extractionPipelineConfig.path))
                ?: throw IllegalArgumentException("Failed to read pipeline configuration from '${extractionPipelineConfig.path}'.")
            if (ingestionConfig.schema != schema.name) {
                throw IllegalArgumentException("Schema name in pipeline configuration '${ingestionConfig.schema}' does not match schema name '${schema.name}'.")
            }
            schema.addIngestionPipeline(extractionPipelineName, ingestionConfig)
        }

        /* Cache and return connection. */
        this.schemas[schema.name] = schema
    }

    /**
     * Sets the name for a [SchemaConfig], then calls load.
     *
     * @param name The name of the schema.
     * @param config The [SchemaConfig] to which to assign the name and load.
     */
    fun load(name: String, config: SchemaConfig) {
        config.name = name
        load(config)
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
