package org.vitrivr.engine.core.api.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.jakewharton.picnic.table
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import kotlin.math.roundToInt

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class SchemaCommand: NoOpCliktCommand(
    name = "schema",
    help = "Groups commands that act on a vitrivr engine schema.",
    epilog = "Schema related commands usually have the form: schema <command> <name>, e.g., `schema list` Check help for command specific parameters.",
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {

    init {
        subcommands(
            List(),
            About(),
            Initialize()
        )
    }

    /**
     * [CliktCommand] to list all schemas registered with this vitrivr instance.
     */
    class List: CliktCommand(name = "list", help = "Lists all schemas that are registered with this vitrivr instance.") {
        override fun run() {
            val table = table {
                cellStyle { border = true; paddingLeft = 1; paddingRight = 1 }
                header {
                    row {
                        cell("Name")
                        cell("Fields")
                        cell("Connection")
                        cell("Initialized (%)")
                    }
                }
                body {
                    val schemas = SchemaManager.listSchemas()
                    for (schema in schemas) {
                        var total = 1.0f
                        var initialized = if (schema.connection.getRetrievableInitializer().isInitialized()) {
                            1.0f
                        } else {
                            0.0f
                        }
                        for (field in schema.fields()) {
                            total += 1.0f
                            if (field.getInitializer().isInitialized()) {
                                initialized += 1.0f
                            }
                        }

                        row {
                            cell(schema.name)
                            cell(schema.fields().size)
                            cell(schema.connection.description())
                            cell("${(initialized / total).roundToInt()}%")
                        }
                    }
                }
            }
            println(table)
        }
    }

    /**
     * [CliktCommand] to list all fields in a schema registered with this vitrivr instance.
     */
    class About: CliktCommand(name = "about", help = "Lists all fields that are registered with the specified vitrivr schema.") {

        /** The schema name affected by this [About]. */
        private val schemaName: String by argument(name = "schema", help = "The schema name targeted by the command.")

        /**
         * Executes the command.
         */
        override fun run() {
            val schema = SchemaManager.getSchema(this.schemaName)
            if (schema != null) {
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
                        for (field in schema.fields()) {
                            row {
                                cell(field.fieldName)
                                cell(field.analyser.analyserName)
                                cell(field.analyser.contentClass.simpleName)
                                cell(field.analyser.descriptorClass.simpleName)
                                cell(schema.connection.description())
                                cell(field.getInitializer().isInitialized())
                            }
                        }
                    }
                }
                println(table)
            }
        }
    }

    /**
     *
     */
    class Initialize : CliktCommand(name = "init", help = "Initializes the schema using the database connection") {
        /** The schema name affected by this [About]. */
        private val schemaName: String by argument(name = "schema", help = "The schema name targeted by the command.")

        override fun run() {
            val schema = SchemaManager.getSchema(this.schemaName)
            var initialized = 0
            if (schema != null) {
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
                println("Successfully initialized schema '${schemaName}'; created $initialized entities.")
            } else {
                println("Failed to initialize schema '${schemaName}', because it does not seem to exist.")
            }
        }
    }
}