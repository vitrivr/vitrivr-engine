package org.vitrivr.engine.query.operators.transform.benchmark

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.*
import java.nio.file.Path
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class BenchmarkLogger(val logfile: Path) : Runnable {
    private val logger: KLogger = KotlinLogging.logger {}

    private val queue: BlockingQueue<BenchmarkMessage> = LinkedBlockingQueue()

    infix fun log(message: BenchmarkMessage) {
        queue.add(message)
    }

    override fun run() {
        while (true) {

            val log = queue.take()
            logger.info { log }


            FileOutputStream(File(logfile.toString()), true).bufferedWriter().use { writer ->
                writer.appendLine("${Json.encodeToJsonElement(log).toString()},")
                writer.close()
            }
        }
    }
}