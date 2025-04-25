package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

private val logger = KotlinLogging.logger {}

/**
 * A [Transformer] that converts descriptors to content elements for further processing.
 *
 * @author Fynn Faber
 * @version 1.1.0
 */
class DescriptorAsContentTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer = Instance(
        input = input,
        name = name,
        contentFactory = (context as IndexContext).contentFactory,
        fieldName = context[name, "field"]  ?: throw IllegalArgumentException("The descriptor as content transformer requires a field name.")
    )

    private class Instance(override val input: Operator<out Retrievable>, override val name: String, val contentFactory: ContentFactory, val fieldName : String) : Transformer {
        override fun toFlow(scope: CoroutineScope) = this.input.toFlow(scope).map { retrievable : Retrievable ->
            /* Extract content. */
            val newContent = retrievable.descriptors.filter {
                descriptor -> descriptor.field?.fieldName == fieldName
            }.flatMap{ descriptor -> this@Instance.convertDescriptor(descriptor) }

            /* Emit retrievable. */
            retrievable.copy(content = retrievable.content + newContent)
        }

        /**
         * Converts a [Descriptor] to a list of [TextContent]s.
         *
         * @param descriptor [Descriptor] to convert
         * @return [List] of [TextContent]
         */
        private fun convertDescriptor(descriptor: Descriptor<*>): List<TextContent> {
            return when (descriptor) {
                is StringDescriptor -> listOf(contentFactory.newTextContent(descriptor.value.value))
                is TextDescriptor -> listOf(contentFactory.newTextContent(descriptor.value.value))
                is FileSourceMetadataDescriptor -> listOf(contentFactory.newTextContent(descriptor.path.value))
                is StructDescriptor<*> -> descriptor.values().map{ (k,v) -> contentFactory.newTextContent(v.toString()) }
                else -> emptyList()
            }
        }
    }
}