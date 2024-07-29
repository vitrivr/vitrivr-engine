package org.vitrivr.engine.base.features.external.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractBatchedExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import java.util.*
import java.util.logging.Logger

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An abstract [Extractor] implementation that is suitable for [ExternalFesAnalyser]s.
 *
 * @param D The type of the [Descriptor] to extract.
 * @param C The type of the [ContentElement] to extract from.
 * @param A The type of the [ExternalFesAnalyser] to use.
 */
abstract class FesExtractor<D:Descriptor,C:ContentElement<*>, A:ExternalFesAnalyser<C,D>>(
    input: Operator<Retrievable>,
    field: Schema.Field<C, D>?,
    bufferSize: Int,
    private val contentSources : Set<String>?
) : AbstractBatchedExtractor<C, D>(input, field, bufferSize) {


    /**
     * Checks if the [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean {
        val analyser = field!!.analyser as A
        return retrievable.findContent { contentItem ->
            analyser.contentClasses.any { contentClass ->
                contentClass.isInstance(contentItem)
            }
        }.isNotEmpty()

    }

    /**
     * Extracts descriptors from the given [Retrievable]s.
     *
     * @param retrievables The [Retrievable]s to extract descriptors from.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievables: List<Retrievable>): List<List<D>> {
        val analyser = field!!.analyser as A

        val allContent : List<List<C>> = retrievables.map { retrievable ->
            val authors = retrievable.filteredAttribute(ContentAuthorAttribute::class.java)
            val retrievableContentIds : Set<UUID>? = contentSources?.flatMap { authors?.getContentIds(it)?: emptySet() }?.toSet()
            retrievable.findContent { contentItem ->
                analyser.contentClasses.any { contentClass ->
                    contentClass.isInstance(contentItem) && retrievableContentIds?.contains(contentItem.id) ?: true
                }
            }.map{ it as C}
        }

        val idString: String = retrievables.joinToString(", ") { it.id.toString() }

        logger.debug { "Extracting descriptors for field ${this.field?.fieldName} from ${retrievables.size} retrievables (${allContent.flatten().size} content elements total): $idString" }

        val allDescriptors: List<List<D>>
        try {
            allDescriptors = analyser.analyse(allContent, field!!.parameters)
        } catch (e: Exception) {
            logger.error(e) { "Error during extraction of descriptors in field ${field!!.fieldName}." }
            throw e
        }

        logger.debug { "Extracted ${allDescriptors.flatten().size} descriptors from ${allContent.flatten().size} content elements total." }

        return allDescriptors.mapIndexed { index, descriptors ->
            val retrievableId = retrievables[index].id
            descriptors.map { assignRetrievableId(it, retrievableId) }
        }

    }

    abstract fun assignRetrievableId(descriptor: D, retrievableId: RetrievableId): D

}