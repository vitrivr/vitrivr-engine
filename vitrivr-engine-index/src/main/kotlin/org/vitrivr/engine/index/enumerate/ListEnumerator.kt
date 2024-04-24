package org.vitrivr.engine.index.enumerate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.Source
import java.util.LinkedList
import java.util.stream.Stream

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
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     */
    override fun newOperator(name: String, context: IndexContext): Enumerator {
        return Instance(context)
    }
    /**
     * Creates a new [Enumerator] instance from this [ListEnumerator].
     *
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     * @param inputs Is ignored.
     */
    override fun newOperator(name: String, context: IndexContext, inputs: Stream<*>?): Enumerator {
        return newOperator(name, context)
    }

    /**
     * The [Enumerator] returned by this [FileSystemEnumerator].
     */
    class Instance(private val context: IndexContext) : Enumerator {

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
