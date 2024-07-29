package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import java.time.Year
import javax.swing.text.AbstractDocument.Content

private val logger = KotlinLogging.logger {}

/**
 * A [Transformer] that takes an input template with placeholders and inserts content from fields in their place.
 *
 * @author Laura Rettig
 * @version 1.0.0
 */
class ContentMergingTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        val template = context[name, "template"] ?: throw IllegalArgumentException("The content merging transformer requires a template.")
        val regex = "\\$\\{([^}]+)\\}".toRegex()
        val contentFields = regex.findAll(template).map { it.groupValues[1] }.toList()
        val defaultValue = context[name, "defaultValue"] ?: ""
        return Instance(
            input = input,
            contentFactory = (context as IndexContext).contentFactory,
            template = template,
            contentFields = contentFields,
            defaultValue = defaultValue,
            name = name
        )
    }

    private class Instance(override val input: Operator<out Retrievable>, val contentFactory: ContentFactory, val template: String, val contentFields: List<String>, val defaultValue: String, val name: String) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            input.toFlow(scope).collect { retrievable: Retrievable ->
                var mergedContent = template

                contentFields.forEach { fieldName ->
                    val placeholder = "\${${fieldName}}"
                    val contentIds = retrievable.filteredAttribute(ContentAuthorAttribute::class.java)?.getContentIds(fieldName)

                    val fieldContent = StringBuilder()
                    contentIds?.forEach{ id ->
                        retrievable.content.find {
                            it.id == id && it.type == ContentType.TEXT
                        }?.content?.let {
                            fieldContent.append(it)
                        }
                    }

                    val finalContent = if (fieldContent.isEmpty()) defaultValue else fieldContent.toString()
                    mergedContent = mergedContent.replace(placeholder, finalContent)
                }

                if (mergedContent.isNotBlank()) {
                    val content = contentFactory.newTextContent(mergedContent.trim())
                    retrievable.addContent(content)
                    retrievable.addAttribute(ContentAuthorAttribute(content.id, name))
                    logger.debug { "Contents from retrievable ${retrievable.id} have been merged into a single content element using template." }
                }
                emit(retrievable)
            }
        }
    }
}