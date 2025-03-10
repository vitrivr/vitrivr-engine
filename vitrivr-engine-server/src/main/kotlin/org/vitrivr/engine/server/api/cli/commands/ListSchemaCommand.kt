package org.vitrivr.engine.server.api.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.jakewharton.picnic.table
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.server.api.cli.Cli.MainCommand
import kotlin.math.roundToInt


/**
 * [CliktCommand] to list all schemas registered with this vitrivr instance.
 */
class ListSchemaCommand(private val manager: SchemaManager) : CliktCommand(name = "list") {

    /**
     * Returns the help message for the [MainCommand].
     */
    override fun help(context: Context): String {
        return "Lists all schemas that are registered with this vitrivr instance."
    }

    /**
     * Executes the command.
     */
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
                val schemas = this@ListSchemaCommand.manager.listSchemas()
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
                        cell("${((initialized * 100) / total).roundToInt()}%")
                    }
                }
            }
        }
        println(table)
    }
}