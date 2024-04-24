package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext
import java.util.stream.Stream
import java.util.zip.CheckedInputStream

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
     */
    fun newOperator(name: String, context: IndexContext): Enumerator{
        return newOperator(name, context, null)
    }

    fun newOperator(name: String, context: IndexContext, inputs: Stream<*>? = null): Enumerator
}
