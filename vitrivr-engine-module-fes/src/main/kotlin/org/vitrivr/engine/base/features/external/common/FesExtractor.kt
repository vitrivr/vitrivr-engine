package org.vitrivr.engine.base.features.external.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractBatchedExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.operators.Operator
import java.util.logging.Logger

private val logger: KLogger = KotlinLogging.logger {}

abstract class FesExtractor<D:Descriptor,C:ContentElement<*>, A:ExternalFesAnalyser<C,D>>(
        input: Operator<Retrievable>,
        field: Schema.Field<C, D>,
        persisting: Boolean = true,
        bufferSize: Int
) : AbstractBatchedExtractor<C, D>(input, field, persisting, bufferSize) {


    override fun matches(retrievable: Retrievable): Boolean {
        val analyser = field.analyser as A
        return retrievable.filteredAttributes(ContentAttribute::class.java).any { contentItem ->
            analyser.contentClasses.any { contentClass ->
                contentClass.isInstance(contentItem.content)
            }.also {
                if (!it) {
                    logger.warn("Content class ${contentItem.content.javaClass.name} does not match any of the expected content classes for analyser ${analyser.javaClass.name}")
                }
            }
        }

    }

    override fun extract(retrievables: List<Retrievable>): List<List<D>> {
        val analyser = field.analyser as A

        val allContent : List<List<C>> = retrievables.map { retrievable ->
            retrievable.filteredAttributes(ContentAttribute::class.java).map { it.content }.filter{ contentItem -> analyser.contentClasses.any { contentClass -> contentClass.isInstance(contentItem) } }.map{ it as C}
        }
        logger.debug { "Extracting descriptors from ${retrievables.size} retrievables (${allContent.flatten().size} content elements total)." }

        val allDescriptors = analyser.analyse(allContent, field.parameters)

        logger.debug { "Extracted ${allDescriptors.flatten().size} descriptors from ${allContent.flatten().size} content elements total." }

        return allDescriptors.mapIndexed { index, descriptors ->
            val retrievableId = retrievables[index].id
            descriptors.map { assignRetrievableId(it, retrievableId) }
        }

    }

    abstract fun assignRetrievableId(descriptor: D, retrievableId: RetrievableId): D

}