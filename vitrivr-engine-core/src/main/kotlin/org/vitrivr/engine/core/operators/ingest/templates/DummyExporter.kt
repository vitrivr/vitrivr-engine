package org.vitrivr.engine.core.operators.ingest.templates

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.Transformer

private val logger: KLogger = KotlinLogging.logger {}

/***
 * A Template for a [Transformer].
 *
 * @author Raphael Waltenspül
 * @version 1.0
 */
class DummyExporter(
    override val  input: Operator<Ingested>,
    val parameters: Map<String, Any>
) : Exporter {
    override fun toFlow(scope: CoroutineScope):  Flow<Ingested> {
        return this.input.toFlow(scope).map { value: Ingested ->
            logger.info { "Performed Dummy Exporter with options ${parameters} on ${value}" }
            value
        }
    }
}