package org.vitrivr.engine.core.operators.ingest.templates

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.Source

private val logger: KLogger = KotlinLogging.logger {}

/***
 * A Template for a [Enumerator].
 *
 * @author Raphael Waltenspül
 * @version 1.0
 */
class DummyEnumerator() : Enumerator {
    override fun toFlow(scope: CoroutineScope): Flow<Source> {
        return channelFlow { logger.info { "Performed Dummy Enumerator with options" } }
    }
}