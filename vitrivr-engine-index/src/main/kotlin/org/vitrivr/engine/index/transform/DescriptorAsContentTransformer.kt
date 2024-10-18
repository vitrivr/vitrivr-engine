package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

private val logger = KotlinLogging.logger {}

/**
 * A [Transformer] that converts descriptors to content elements for further processing.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class DescriptorAsContentTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {

        return Instance(
            input = input,
            contentFactory = (context as IndexContext).contentFactory,
            fieldName = context[name, "field"]  ?: throw IllegalArgumentException("The descriptor as content transformer requires a field name."),
            name = name
        )
    }

    private class Instance(override val input: Operator<out Retrievable>, val contentFactory: ContentFactory, val fieldName : String, val name: String) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            input.toFlow(scope).collect {
                retrievable : Retrievable ->
                retrievable.descriptors.filter{
                    descriptor ->
                    descriptor.field?.fieldName == fieldName
                }.forEach{ descriptor ->
                    val pairs = processDescriptor(descriptor)
                    for (pair in pairs) {
                        val content = pair.second
                        retrievable.addContent(content)
                        for (key in pair.first) {
                            val attribute = ContentAuthorAttribute(content.id, key)
                            retrievable.addAttribute(attribute)
                        }
                        logger.debug { "Descriptor ${descriptor.id} of retrievable ${retrievable.id} has been converted to content element." }
                    }
                }
                emit(retrievable)
            }
        }

        private fun processDescriptor(descriptor: Descriptor<*>): List<Pair<Set<String>, ContentElement<*>>> {
            return when (descriptor) {
                is StringDescriptor -> listOf(Pair(setOf(name), contentFactory.newTextContent(descriptor.value.value)))
                is TextDescriptor -> listOf(Pair(setOf(name), contentFactory.newTextContent(descriptor.value.value)))
                is FileSourceMetadataDescriptor -> listOf(Pair(setOf(name), contentFactory.newTextContent(descriptor.path.value)))
                is AnyMapStructDescriptor -> {
                    descriptor.values().map{
                        entry ->
                        Pair(setOf(name, "$name.${entry.key}"), contentFactory.newTextContent(entry.value.toString()))
                    }
                }
                else -> throw IllegalArgumentException("Descriptor type not supported.")
            }
        }
    }
}