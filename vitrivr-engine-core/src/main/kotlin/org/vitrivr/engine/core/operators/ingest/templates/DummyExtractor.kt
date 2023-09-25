package org.vitrivr.engine.core.operators.ingest.templates

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor


private val logger: KLogger = KotlinLogging.logger {}

/***
 * A Template for a [Extractor].
 *
 * @author Raphael Waltenspül
 * @version 1.0
 */
class DummyExtractor(override val  input: Operator<Ingested>) : Extractor<ContentElement<*>, Descriptor> {
    override val field: Schema.Field<ContentElement<*>, Descriptor>
        get() = TODO("Not yet implemented")
    override val persisting: Boolean
        get() = TODO("Not yet implemented")

    override fun toFlow(scope: CoroutineScope): Flow<Ingested> {
        return channelFlow { logger.info { "Performed Dummy Extractor with options" } }
    }
}