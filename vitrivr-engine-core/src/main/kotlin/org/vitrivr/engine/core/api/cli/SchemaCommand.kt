package org.vitrivr.engine.core.api.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.jakewharton.picnic.table
import org.vitrivr.engine.core.model.metamodel.SchemaManager

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
            About()
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
                    }
                }
                body {
                    val schemas = SchemaManager.listSchemas()
                    for (schema in schemas) {
                        row {
                            cell(schema.name)
                            cell(schema.fields().size)
                            cell(schema.connection.description())
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
        protected val schemaName: String by argument(name = "schema", help = "The schema name targeted by the command.")

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
                        }
                    }
                    body {
                        for (field in schema.fields()) {
                            row {
                                cell(field.fieldName)
                                cell(field.analyser.analyserName)
                                cell(field.analyser.contentClass.qualifiedName)
                                cell(field.analyser.descriptorClass.qualifiedName)
                                cell(schema.connection.description())
                            }
                        }
                    }
                }
                println(table)
            }
        }
    }
}