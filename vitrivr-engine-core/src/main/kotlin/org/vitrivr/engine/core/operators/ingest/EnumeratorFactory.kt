package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.source.MediaType
import java.util.stream.Stream

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
    fun newEnumerator(name: String, parameters: Map<String, String>, mediaTypes: List<MediaType>): Enumerator {
        return newEnumerator(name, parameters, mediaTypes, null)
    }

    fun newEnumerator(name: String, parameters: Map<String, String>, mediaTypes: List<MediaType>, inputs: Stream<*>? = null): Enumerator
}
