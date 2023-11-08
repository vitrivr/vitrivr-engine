package org.vitrivr.engine.core.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * An abstract [Extractor] implementation that is suitable for most default [Extractor] implementations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractExtractor<C : ContentElement<*>, D : Descriptor>(final override val input: Operator<Retrievable>, final override val field: Schema.Field<C, D>, final override val persisting: Boolean = true, private val bufferSize: Int = 100) :
    Extractor<C, D> {

    /**
     * A default [Extractor] implementation. It executes the following steps:
     *
     * - It checks if an incoming [Retrievable] matches the requirements posed by this [Extractor]
     * - If so, it generates the [Descriptor]s for the [Retrievable]
     * - Depending on configuration, it appends [Descriptor] and/or persists them.
     *
     * @return [Flow] of [Retrievable]
     */
    final override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        /* The [DescriptorWriter] used by this [AbstractExtractor]. */
        val writer: DescriptorWriter<D> by lazy { this.field.getWriter() }

        /* The buffer used for writing descriptors. */
        val buffer = LinkedList<D>()

        /* Prepare and return flow. */
        return this.input.toFlow(scope).onEach { retrievable ->
            if (this.matches(retrievable)) {
                /* Perform extraction. */
                val descriptors = extract(retrievable)

                /* Append descriptor. */
                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    for (d in descriptors) {
                        retrievable.addDescriptor(d)
                    }
                }

                /* Persist descriptor. */
                if (this.persisting) {
                    /* Add descriptors to buffer. */
                    for (d in descriptors) {
                        buffer.add(d)
                    }

                    /* Persist buffer if necessary. */
                    if (buffer.size >= this.bufferSize) {
                        writer.addAll(buffer)
                        buffer.clear()
                    }
                }
            }
        }.onCompletion {
            /* Persist buffer if necessary. */
            if (buffer.isNotEmpty()) {
                writer.addAll(buffer)
                buffer.clear()
            }
        }
    }

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    protected abstract fun matches(retrievable: Retrievable): Boolean

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    protected abstract fun extract(retrievable: Retrievable): List<D>
}