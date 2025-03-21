package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
 * @version 1.1.0
 */
abstract class AbstractBatchedExtractor<C : ContentElement<*>, D : Descriptor<*>>
    private constructor(
        final override val input: Operator<Retrievable>,
        final override val analyser: Analyser<C, D>,
        final override val field: Schema.Field<C, D>? = null,
        final override val name: String,
        private val bufferSize: Int
) :
    Extractor<C, D> {

    constructor(input: Operator<Retrievable>, analyser: Analyser<C, D>, field: Schema.Field<C, D>, bufferSize: Int = 100) : this(
        input,
        analyser,
        field,
        field.fieldName,
        bufferSize
    )

    constructor(input: Operator<Retrievable>, analyser: Analyser<C, D>, name: String, bufferSize: Int = 100) : this(
        input,
        analyser,
        null,
        name,
        bufferSize
    )




    companion object {
        const val BATCH_SIZE_KEY = "batchSize"
    }

    init {
        require(field == null || this.field.analyser == this.analyser) { "Field and analyser do not match! This is a programmer's error!" }
    }

    /** The [KLogger] instance used by this [AbstractExtractor]. */
    protected val logger: KLogger = KotlinLogging.logger {}


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
    final override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
        val batch = mutableListOf<Retrievable>()
        this@AbstractBatchedExtractor.input.toFlow(scope).collect { retrievable ->
            if (this@AbstractBatchedExtractor.matches(retrievable)) {
                batch.add(retrievable)
            } else {
                send(retrievable)
            }
            if (batch.size >= bufferSize) {
                extractBatchAndAppend(batch).forEach { send(it) }
                batch.clear()
            }
        }

        /* Emit any remaining items in the batch */
        if (batch.isNotEmpty()) {
            extractBatchAndAppend(batch).forEach { send(it) }
            batch.clear()
        }
    }


    /**
     * Internal method to perform extraction on batch of [Retrievable].
     **
     * @param batch The list of [Retrievable] to process.
     * @return List of lists of resulting [Descriptor]s, one list for each [Retrievable].
     */
    protected abstract fun extract(batch: List<Retrievable>): List<List<D>>

    /**
     * Internal method to perform extraction on batch of [Retrievable] and append resulting [Descriptor]s
     *
     * @param batch The list of [Retrievable] to process.

     * @return List of lists of resulting [Descriptor]s, one list for each [Retrievable].
     */
    private fun extractBatchAndAppend(batch: List<Retrievable>): List<Retrievable> {
        val allDescriptors = try {
            extract(batch)
        } catch (e: Exception) {
            logger.error(e) { "Error during batched extraction: ${e.message}" }
            emptyList()
        }
        return batch.mapIndexed { i, r ->
            val sourceAttribute = DescriptorAuthorAttribute()
            val descriptors = allDescriptors.getOrNull(i) ?: emptyList()
            descriptors.forEach { d -> sourceAttribute.add(d, name) }
            r.copy(descriptors = r.descriptors + descriptors, attributes = r.attributes + sourceAttribute)
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
}