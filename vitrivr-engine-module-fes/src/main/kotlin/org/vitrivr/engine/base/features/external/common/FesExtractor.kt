package org.vitrivr.engine.base.features.external.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractBatchedExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator
import java.util.logging.Logger

private val logger: KLogger = KotlinLogging.logger {}

abstract class FesExtractor<D:Descriptor,C:ContentElement<*>, A:ExternalFesAnalyser<C,D>>(
        input: Operator<Retrievable>,
        field: Schema.Field<C, D>?,
        bufferSize: Int
) : AbstractBatchedExtractor<C, D>(input, field, bufferSize) {


    override fun matches(retrievable: Retrievable): Boolean {
        val analyser = field!!.analyser as A
        return retrievable.findContent { contentItem ->
            analyser.contentClasses.any { contentClass ->
                contentClass.isInstance(
                    contentItem.content
                )
            }
        }.isNotEmpty()

    }

    override fun extract(retrievables: List<Retrievable>): List<List<D>> {
        val analyser = field!!.analyser as A

        val allContent : List<List<C>> = retrievables.map { retrievable ->
            retrievable.findContent { contentItem ->
                analyser.contentClasses.any { contentClass ->
                    contentClass.isInstance(
                        contentItem.content
                    )
                }
            }.map{ it as C}
        }
        logger.debug { "Extracting descriptors from ${retrievables.size} retrievables (${allContent.flatten().size} content elements total)." }

        val allDescriptors = analyser.analyse(allContent, field!!.parameters)

        logger.debug { "Extracted ${allDescriptors.flatten().size} descriptors from ${allContent.flatten().size} content elements total." }

        return allDescriptors.mapIndexed { index, descriptors ->
            val retrievableId = retrievables[index].id
            descriptors.map { assignRetrievableId(it, retrievableId) }
        }

    }

    abstract fun assignRetrievableId(descriptor: D, retrievableId: RetrievableId): D

}