package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * An abstract [Extractor] implementation that is suitable for [Extractor] implementations which extract descriptors in batches of multiple retrievables.
 *
 * @author Fynn Faber
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractBatchedExtractor<C : ContentElement<*>, D : Descriptor>(final override val input: Operator<Retrievable>, final override val field: Schema.Field<C, D>?, private val bufferSize: Int = 100) :
    Extractor<C, D> {
    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * A default [Extractor] implementation for batched extraction. It executes the following steps:
     *
     * - It checks if the [Retrievable] matches the [Extractor] by calling [matches].
     * - If the [Retrievable] matches, it is added to a buffer.
     * - If the buffer reaches a certain size, the [Extractor] is called to extract descriptors from the buffer.
     * - The descriptors are then added to the [Retrievable].
     *
     * @return [Flow] of [Retrievable]
     */
    final override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        return flow {
            val batch = mutableListOf<Retrievable>()

            this@AbstractBatchedExtractor.input.toFlow(scope).collect { retrievable ->
                if (retrievable.type == "SOURCE:VIDEO") {
                    logger.info { "Processing video ${retrievable.id} with field ${field?.fieldName}" }
                }
                try {
                    if (this@AbstractBatchedExtractor.matches(retrievable)) {
                        batch.add(retrievable)
                    }
                    else {
                        emit(retrievable)
                    }
                    if (batch.size >= bufferSize) {
                        val descriptors = extract(batch)
                        // zip descriptors and batch
                        for (i in batch.indices) {
                            val r = batch[i]
                            for (d in descriptors[i]) {
                                r.addDescriptor(d)
                            }
                        }
                        emitAll(batch.asFlow())
                        batch.clear()
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error during extraction" }
                    "Error during extraction: $e".let {
                        logger.error { it }
                    }
                }
            }

            // Emit any remaining items in the batch
            if (batch.isNotEmpty()) {
                val descriptors = extract(batch)
                // zip descriptors and batch
                for (i in batch.indices) {
                    val r = batch[i]
                    for (d in descriptors[i]) {
                        r.addDescriptor(d)
                    }
                }
                emitAll(batch.asFlow())
                batch.clear()
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
     * Internal method to perform extraction on batch of [Retrievable].
     **
     * @param retrievables The list of [Retrievable] to process.
     * @return List of lists of resulting [Descriptor]s, one list for each [Retrievable].
     */
    protected abstract fun extract(retrievables: List<Retrievable>): List<List<D>>

}