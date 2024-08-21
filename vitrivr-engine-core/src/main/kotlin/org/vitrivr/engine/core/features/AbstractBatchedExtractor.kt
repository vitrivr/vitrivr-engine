package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * An abstract [Extractor] implementation that is suitable for [Extractor] implementations which extract descriptors in batches of multiple retrievables.
 *
 * @author Fynn Faber
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractBatchedExtractor<C : ContentElement<*>, D : Descriptor<*>>(
    final override val input: Operator<Retrievable>, final override val analyser: Analyser<C, D>, final override val field: Schema.Field<C, D>?,
    final override val name: String = field!!.fieldName, private val bufferSize: Int = 100
) : Extractor<C, D> {
    private val logger: KLogger = KotlinLogging.logger {}


    init {
        require(field == null || this.field.analyser == this.analyser) { "Field and analyser do not match! This is a programmer's error!" }
    }

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
                try {
                    if (this@AbstractBatchedExtractor.matches(retrievable)) {
                        batch.add(retrievable)
                    }
                    else {
                        emit(retrievable)
                    }
                    if (batch.size >= bufferSize) {
                        logger.debug { "Batch size reached for field ${field?.fieldName}, extracting descriptors" }
                        val descriptors = extract(batch)
                        batch.forEachIndexed { i, r ->
                            val sourceAttribute = DescriptorAuthorAttribute()
                            descriptors[i].forEach { d ->
                                r.addDescriptor(d)
                                sourceAttribute.add(d, name)
                            }
                            r.addAttribute(sourceAttribute)
                        }
                        emitAll(batch.asFlow())
                        batch.clear()
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error during extraction" }
                }
            }

            // Emit any remaining items in the batch
            if (batch.isNotEmpty()) {
                val descriptors = extract(batch)
                batch.forEachIndexed { i, r ->
                    descriptors[i].forEach { d ->
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
     * By default, a [Retrievable] matches this [Extractor] if it contains at least one [ContentElement] that matches the [Analyser.contentClasses].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    protected open fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { content ->
        this.analyser.contentClasses.any { it.isInstance(content) }
    }

    /**
     * Internal method to perform extraction on batch of [Retrievable].
     **
     * @param retrievables The list of [Retrievable] to process.
     * @return List of lists of resulting [Descriptor]s, one list for each [Retrievable].
     */
    protected abstract fun extract(retrievables: List<Retrievable>): List<List<D>>

}