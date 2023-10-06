package org.vitrivr.engine.core.operators.ingest.templates

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.Source

private val logger: KLogger = KotlinLogging.logger {}

/***
 * A Template for a [Enumerator].
 *
 * @author Raphael Waltenspül
 * @version 1.0
 */
class DummyEnumerator(val parameters: Map<String,Any> ) : Enumerator {
    override fun toFlow(scope: CoroutineScope): Flow<Source> {
        return channelFlow { logger.info { "Performed Dummy Enumerator with options ${parameters}" } }
    }
}