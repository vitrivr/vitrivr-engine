package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
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
abstract class AbstractBatchedExtractor<C : ContentElement<*>, D : Descriptor<*>>
    private constructor(
        final override val input: Operator<Retrievable>,
        final override val analyser: Analyser<C, D>,
        final override val field: Schema.Field<C, D>? = null,
        protected val contentSources : Set<String>? = null,
        final override val name: String,
        private val bufferSize: Int
) :
    Extractor<C, D> {

    constructor(input: Operator<Retrievable>, analyser: Analyser<C, D>, contentSources : Set<String>?, field: Schema.Field<C, D>, bufferSize: Int = 100) : this(
        input,
        analyser,
        field,
        contentSources,
        field.fieldName,
        bufferSize
    )

    constructor(input: Operator<Retrievable>, analyser: Analyser<C, D>, contentSources : Set<String>?, name: String, bufferSize: Int = 100) : this(
        input,
        analyser,
        null,
        contentSources,
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

    /**
     * Filters the content of a [Retrievable] based on the [ContentAuthorAttribute] and the [contentSources] parameter.
     *
     * @param retrievable [Retrievable] to extract content from.
     */
    @Suppress("UNCHECKED_CAST")
    protected fun filterContent(retrievable: Retrievable): List<C> {
        val contentIds = this.contentSources?.let {
            retrievable.filteredAttribute(ContentAuthorAttribute::class.java)?.getContentIds(it)
        }
        return retrievable.content.filter { content ->
            if (this.analyser.contentClasses.none { it.isInstance(content) }) return@filter false
            if (contentIds == null) {
                return@filter true
            } else {
                return@filter contentIds.contains(content.id)
            }
        }.map { it as C }
    }
}