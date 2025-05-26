package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * Abstract class for implementing an external feature retriever.
 *
 * @param C Type of [ContentElement] that this external retriever operates on.
 * @param D Type of [Descriptor] produced by this external retriever.
 *
 * @see [AbstractRetriever]
 *
 * @author Rahel Arnold
 * @version 1.0.1
 */
abstract class AbstractRetriever<C : ContentElement<*>, D : Descriptor<*>>(override val field: Schema.Field<C, D>, val query: Query, val context: Context) : Retriever<C, D> {

    /** The [DescriptorReader] instance used by this [AbstractRetriever]. */
    protected val reader: DescriptorReader<D> by lazy { this.field.getReader() }

    /** The [KLogger] instance used by this [AbstractRetriever]. */
    protected val logger: KLogger = KotlinLogging.logger {}

    /**
     * Simplest implementation of the retrieval logic simply hand the [Query] to the [DescriptorReader] and emit the results.
     *
     * @param scope The [CoroutineScope] to execute the resulting [Flow] in.
     */
    override fun toFlow(scope: CoroutineScope) = flow {
        this@AbstractRetriever.reader.queryAndJoin(this@AbstractRetriever.query).forEach {
            emit(it)
        }
    }
}