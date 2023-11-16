package org.vitrivr.engine.server.api.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.jakewharton.picnic.table
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.config.IndexConfig
import org.vitrivr.engine.core.config.pipeline.PipelineBuilder
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import java.nio.file.Paths

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class SchemaCommand(private val schema: Schema, private val server: ExecutionServer) : NoOpCliktCommand(
    name = schema.name,
    help = "Groups commands related to a specific the schema '${schema.name}'.",
    epilog = "Schema related commands usually have the form: <schema> <command>, e.g., `vitrivr about` Check help for command specific parameters.",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {


    init {
        this.subcommands(
            About(),
            Initialize(),
            Extract(this.schema, this.server)
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
                            cell(field.analyser.contentClass.simpleName)
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
    inner class Initialize : CliktCommand(name = "init", help = "Initializes the schema using the database connection.") {
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

    inner class Extract(private val schema: Schema, private val server: ExecutionServer) : CliktCommand(name = "extract", help = "Extracts data from a source and stores it in the schema.") {
        override fun run() {
            val config = IndexConfig.read(Paths.get(IndexConfig.DEFAULT_PIPELINE_PATH)) ?: return
            val pipelineBuilder = PipelineBuilder.forConfig(this.schema, config)
            val pipeline = pipelineBuilder.getPipeline()
            this.server.extract(pipeline)
        }
    }
}