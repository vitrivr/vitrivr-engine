package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext

/**
 * A factory object for a specific [Enumerator] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface EnumeratorFactory {
    /**
     * Creates a new [Enumerator] instance from this [EnumeratorFactory].
     *
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(context: IndexContext, parameters: Map<String, String> = emptyMap()): Enumerator
}