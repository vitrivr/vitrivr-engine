package org.vitrivr.engine.query.operators.transform.benchmark

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.logging.log4j.message.Message
import org.vitrivr.engine.query.aggregate.logger
import java.nio.file.Path
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class BenchmarkLogger(logfile: Path) : Runnable {
    private val logger: KLogger = KotlinLogging.logger {}

    private val queue: BlockingQueue<BenchmarkMessage> = LinkedBlockingQueue()

    infix fun log(message: BenchmarkMessage) {
        queue.add(message)
    }

    override fun run() {
        while (true) {
            try {
                val log = queue.take()
                logger.info { log }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}