package org.vitrivr.engine.core.features

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.CONTENT_AUTHORS_KEY
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * An abstract [Extractor] implementation that is suitable for most default [Extractor] implementations.
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
abstract class AbstractExtractor<C : ContentElement<*>, D : Descriptor<*>>(final override val input: Operator<Retrievable>, final override val analyser: Analyser<C, D>, final override val field: Schema.Field<C, D>? = null, protected val parameters: Map<String, String>) : Extractor<C, D> {
    
    protected val logger: KLogger = KotlinLogging.logger {}

    init {
        require(field == null || this.field.analyser == this.analyser) { "Field and analyser do not match! This is a programmer's error!" }
    }

    protected val contentSources : Set<String>?
        get() = parameters[CONTENT_AUTHORS_KEY]?.split(",")?.toSet()

    fun filterContent(retrievable: Retrievable): List<C> {
        val contentIds = this.contentSources?.let {
            retrievable.filteredAttribute(ContentAuthorAttribute::class.java)?.getContentIds(it)
        }
        return retrievable.content.filter { content ->
            this.analyser.contentClasses.any { it.isInstance(content) && (contentIds?.contains(content.id) ?: false) }
        }.map { it as C }
    }

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
            val descriptors = try {
                logger.debug{"Extraction on field ${field?.fieldName} for retrievable: $retrievable" }
                extract(retrievable)
            } catch (e: Throwable) {
                logger.error(e) { "Error during extraction of $retrievable" }
                emptyList()
            }

            /* Append descriptor. */
            logger.trace { "Extracted descriptors for retrievable ($retrievable): $descriptors" }
            for (d in descriptors) {
                retrievable.addDescriptor(d)
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
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    protected abstract fun extract(retrievable: Retrievable): List<D>
}
