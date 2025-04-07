package org.vitrivr.engine.index.enumerate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.util.*
import java.util.stream.Stream

/**
 * A [Enumerator] that allows a caller to explicitly prepare a list of [Source]s to enumerate.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ListEnumerator : EnumeratorFactory {

    /**
     * Creates a new [Enumerator] instance from this [ListEnumerator].
     *
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     */
    override fun newEnumerator(name: String, context: IndexContext, mediaTypes: List<MediaType>): Enumerator {
        val type = context[name, "type"]
        return Instance(type, name)
    }

    /**
     * Creates a new [Enumerator] instance from this [ListEnumerator].
     *
     * @param name The name of the [Enumerator]
     * @param context The [IndexContext] to use.
     * @param inputs Is ignored.
     */
    override fun newEnumerator(name: String, context: IndexContext, mediaTypes: List<MediaType>, inputs: Stream<*>?): Enumerator {
        return newEnumerator(name, context, mediaTypes)
    }

    /**
     * The [Enumerator] returned by this [FileSystemEnumerator].
     */
    class Instance(private val typeName: String? = null, override val name: String) : Enumerator {

        /** List of [Source]s that should be enumerated. */
        private val list: LinkedList<Source> = LinkedList()

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            for (s in this@Instance.list) {
                /* Create source ingested and emit it. */
                val typeName = this@Instance.typeName ?: "SOURCE:${s.type}"
                emit(Ingested(s.sourceId, typeName, attributes = setOf(SourceAttribute(s)), transient = false))
            }

            /* Emit terminal retrievable to signal, that processing has completed. */
            emit(TerminalRetrievable)
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
