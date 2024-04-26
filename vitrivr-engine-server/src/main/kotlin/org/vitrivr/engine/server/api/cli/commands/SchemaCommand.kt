package org.vitrivr.engine.server.api.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jakewharton.picnic.table
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.ingest.IngestionConfig
import org.vitrivr.engine.core.config.ingest.IngestionPipelineBuilder
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.config.pipeline.execution.IndexingPipeline
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.metamodel.Schema
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class SchemaCommand(private val schema: Schema, private val server: ExecutionServer) : NoOpCliktCommand(
    name = schema.name,
    help = "Groups commands related to a specific schema, in this case the schema '${schema.name}'.",
    epilog = "Schema related commands usually have the form: <schema> <command>, e.g., `vitrivr about` Check help for command specific parameters.",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {



    init {
        this.subcommands(
            About(),
            Initialize(),
            Extract(this.schema, this.server),
            Status(this.schema, this.server)
        )
    }

    /**
     * [CliktCommand] to list all fields in a schema registered with this vitrivr instance.
     */
    inner class About : CliktCommand(name = "about", help = "Lists all fields that are registered with the schema.") {

        /**
         * Executes the command.
         */
        override fun run() {
            val table = table {
                cellStyle { border = true; paddingLeft = 1; paddingRight = 1 }
                header {
                    row {
                        cell("Field Name")
                        cell("Analyser")
                        cell("Content Class")
                        cell("Descriptor Class")
                        cell("Connection")
                        cell("Initialized")
                    }
                }
                body {
                    for (field in this@SchemaCommand.schema.fields()) {
                        row {
                            cell(field.fieldName)
                            cell(field.analyser::class.java.simpleName)
                            cell(field.analyser.contentClasses.map { it.simpleName }.joinToString())
                            cell(field.analyser.descriptorClass.simpleName)
                            cell(this@SchemaCommand.schema.connection.description())
                            cell(field.getInitializer().isInitialized())
                        }
                    }
                }
            }
            println(table)
        }
    }

    /**
     * [CliktCommand] to initialize the schema.
     */
    inner class Initialize :
        CliktCommand(name = "init", help = "Initializes the schema using the database connection.") {
        override fun run() {
            val schema = this@SchemaCommand.schema
            var initialized = 0
            var initializer: Initializer<*> = schema.connection.getRetrievableInitializer()
            if (!initializer.isInitialized()) {
                initializer.initialize()
                initialized += 1
            }
            for (field in schema.fields()) {
                initializer = field.getInitializer()
                if (!initializer.isInitialized()) {
                    initializer.initialize()
                    initialized += 1
                }
            }
            println("Successfully initialized schema '${schema.name}'; created $initialized entities.")
        }
    }

    /**
     * [CliktCommand] to start an extraction job.
     */
    inner class Extract(private val schema: Schema, private val executor: ExecutionServer) :
        CliktCommand(name = "extract", help = "Extracts data from a source and stores it in the schema.") {

        private val logger: KLogger = KotlinLogging.logger {}

        /** Path to the configuration file. */
        private val input: Path? by option(
            "-c",
            "--config",
            help = "Path to the extraction configuration."
        ).convert { Paths.get(it) }

        /** Name of the ingestion config as specified on the schema */
        private val name: String? by option(
            "-n",
            "--name",
            help = "The name of the ingestion pipeline configuration to use from the schema"
        )


        override fun run() {
            val pipeline: IndexingPipeline = if (name != null) {
                this.schema.getIngestionPipelineBuilder(name!!).build()
            } else if (input != null) {
                /* Read configuration file. */
                val config = try {
                    IngestionConfig.read(this.input!!)
                } catch (e: Exception) {
                    System.err.println("Failed to read extraction configuration due to error: ${e.message}")
                    return
                }

                /* Check if configuration is valid. */
                if (config == null) {
                    println("Failed to read extraction configuration. No extraction is being started.")
                    return
                }
                IngestionPipelineBuilder(this.schema, config).build()
            } else {
                System.err.println("Requires either -n / --name: Name of the ingestion config defined on the schema or -c / --config the path to a ingestion config")
                return
            }
            val uuid = this.executor.extractAsync(pipeline)
            logger.info { "Started extraction job with UUID $uuid." }
        }
    }

    inner class Status(private val schema: Schema, private val executor: ExecutionServer) :
        CliktCommand(name = "status", help = "Prints indexing status") {
        private val logger: KLogger = KotlinLogging.logger {}

        private val jobId: UUID by option("--job-id", help = "The job id").convert { UUID.fromString(it) }.required()

        override fun run() {
            logger.info { "Status: ${executor.status(jobId)} at ${System.currentTimeMillis()}" }
        }
    }
}
