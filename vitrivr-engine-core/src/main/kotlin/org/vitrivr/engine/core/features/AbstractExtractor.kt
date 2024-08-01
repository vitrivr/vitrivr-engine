package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * An abstract [Extractor] implementation that is suitable for most default [Extractor] implementations.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
abstract class AbstractExtractor<C : ContentElement<*>, D : Descriptor>(final override val input: Operator<Retrievable>, final override val field: Schema.Field<C, D>? = null) : Extractor<C, D> {

    protected val logger: KLogger = KotlinLogging.logger {}

    /**
     * A default [Extractor] implementation. It executes the following steps:
     *
     * - It checks if an incoming [Retrievable] matches the requirements posed by this [Extractor]
     * - If so, it generates the [Descriptor]s for the [Retrievable]
     * - Depending on configuration, it appends [Descriptor] and/or persists them.
     *
     * @return [Flow] of [Retrievable]
     */
    final override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).onEach { retrievable ->
        if (this.matches(retrievable)) {
            /* Perform extraction. */
            logger.debug{"Extraction for retrievable: $retrievable" }
            val descriptors = extract(retrievable)
            descriptors.forEach { descriptor ->

            }

            /* Append descriptor. */
            logger.trace{ "Extracted descriptors for retrievable ($retrievable): $descriptors" }
            for (d in descriptors) {
                retrievable.addDescriptor(d)
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
