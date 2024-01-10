package org.vitrivr.engine.server.api.cli

import com.github.ajalt.clikt.core.*
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.server.api.cli.commands.ListSchemaCommand
import java.io.IOException
import java.util.regex.Pattern

/**
 * Base class for the vitrivr engine CLI.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class Cli(private val manager: SchemaManager) {

    companion object {
        /** The default prompt. */
        private const val PROMPT = "v>"

        /** RegEx for splitting input lines. */
        private val LINE_SPLIT_REGEX: Pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'")
    }

    /** The [MainCommand] instance. */
    private val main = MainCommand()

    /** A flag that indicates, that the CLI is running. */
    @Volatile
    private var stopped: Boolean = true

    /**
     * Registers new [CliktCommand]s with this [Cli].
     *
     * @param commands The [CliktCommand]s to register.
     */
    fun register(vararg commands: CliktCommand) = this.main.subcommands(*commands)

    /**
     * Blocking REPL of the CLI
     */
    fun start() {
        /* Set flag. */
        this.stopped = false

        /* Start terminal. */
        val terminal = try {
            TerminalBuilder.builder().jna(true).build()
        } catch (e: IOException) {
            System.err.println("Could not initialize terminal: ${e.message}. Ending...")
            return
        }

        /* Start CLI loop. */
        val lineReader = LineReaderBuilder.builder().terminal(terminal).appName("vitrivr").build()
        println("vitrivr engine is ready for you to use...")

        while (!this.stopped) {
            /* Catch ^D end of file as exit method */
            val line = try {
                lineReader.readLine(PROMPT).trim()
            } catch (e: EndOfFileException) {
                System.err.println("Could not read from terminal.")
                break
            } catch (e: UserInterruptException) {
                System.err.println("vitrivr engine was interrupted by the user (Ctrl-C).")
                break
            }

            if (line.lowercase() == "help") {
                println(this.main.getFormattedHelp())
                continue
            }
            if (line.isBlank()) {
                continue
            }

            /* Execute command. */
            this.execute(line)

            /* Sleep for a few milliseconds. */
            Thread.sleep(100)
        }
    }

    /**
     * Tries to execute the given CLI command.
     */
    fun execute(command: String) = try {
        val commands = if (command.isEmpty()) {
            emptyList()
        } else {
            val matchList: MutableList<String> = ArrayList()
            val regexMatcher = LINE_SPLIT_REGEX.matcher(command)
            while (regexMatcher.find()) {
                when {
                    regexMatcher.group(1) != null -> matchList.add(regexMatcher.group(1))
                    regexMatcher.group(2) != null -> matchList.add(regexMatcher.group(2))
                    else -> matchList.add(regexMatcher.group())
                }
            }
            matchList
        }
        this.main.parse(commands)
        println()
    } catch (e: Exception) {
        when (e) {
            is PrintHelpMessage -> println(e.context?.command?.getFormattedHelp())
            is NoSuchSubcommand -> System.err.println("Command not found.")
            is MissingArgument -> {
                System.err.println("Missing argument:")
                println(e.context?.command?.getFormattedHelp())
            }

            is MissingOption -> {
                System.err.println("Missing option:")
                println(e.context?.command?.getFormattedHelp())
            }

            is BadParameterValue -> {
                System.err.println("Bad parameter value:")
                println(e.context?.command?.getFormattedHelp())
            }

            is NoSuchOption -> {
                System.err.println("No such option:")
                println(e.context?.command?.getFormattedHelp())
            }

            is UsageError -> println(e.localizedMessage)
            else -> println(e.printStackTrace())
        }
    }

    /**
     * Stops the CLI loop.
     */
    fun stop() {
        this.stopped = true
    }

    /**
     * Wrapper class and single access point to the actual commands.
     */
    inner class MainCommand : NoOpCliktCommand(name = "vitrivr", help = "The base command for all CLI commands.") {
        init {
            subcommands(
                ListSchemaCommand(this@Cli.manager),
                StopCommand()
            )
        }
    }

    /**
     * Stops the [Cli].
     */
    inner class StopCommand : CliktCommand(name = "stop", help = "Stops this CLI.") {
        override fun run() {
            println("Stopping vitrivr engine CLI now...")
            this@Cli.stop()
        }
    }
}