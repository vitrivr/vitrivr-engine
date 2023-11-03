package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.TransformerFactory

private val logger: KLogger = KotlinLogging.logger {}

/***
 * A Template for a [Transformer].
 *
 * @author Raphael Waltenspül
 * @version 1.0
 */
class PassthroughTransformer : TransformerFactory {
    override fun newOperator(input: Decoder, context: IndexContext, parameters: Map<String, String>): Transformer = Instance(input)
    override fun newOperator(input: Transformer, context: IndexContext, parameters: Map<String, String>): Transformer = Instance(input)

    private class Instance(override val input: Operator<ContentElement<*>>) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<ContentElement<*>> {
            return this.input.toFlow(scope).map { value: ContentElement<*> ->
                logger.debug { "Passes through $value" }
                value
            }
        }
    }
}