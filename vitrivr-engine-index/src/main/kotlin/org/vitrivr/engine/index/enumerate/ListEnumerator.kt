package org.vitrivr.engine.index.enumerate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.Source
import java.util.LinkedList

/**
 * A [Enumerator] that allows a caller to explicitly prepare a list of [Source]s to enumerate.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ListEnumerator : EnumeratorFactory {

    /**
     * Creates a new [Enumerator] instance from this [ListEnumerator].
     *
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(context: IndexContext, parameters: Map<String, String>): Enumerator {
        return Instance(context)
    }

    /**
     * The [Enumerator] returned by this [FileSystemEnumerator].
     */
    private class Instance(private val context: IndexContext) : Enumerator {

        /** List of [Source]s that should be enumerated. */
        private val list: LinkedList<Source> = LinkedList()

        override fun toFlow(scope: CoroutineScope): Flow<Source> = flow {
            for (s in this@Instance.list) {
                emit(s)
            }
        }

        /**
         * Enqueues a new [Source].
         *
         * @param source [Source] to add.
         */
        fun add(source: Source) {
            this.list.add(source)
        }
    }
}