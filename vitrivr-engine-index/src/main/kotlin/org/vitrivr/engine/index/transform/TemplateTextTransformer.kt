package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer

private val logger = KotlinLogging.logger {}


/**
 * A [Transformer] that takes an input template with placeholders and inserts content from fields in their place.
 *
 * @author Laura Rettig
 * @version 1.3.0
 */
class TemplateTextTransformer : OperatorFactory {

    companion object {
        /** [Regex] used by [TemplateTextTransformer]. */
        private val TEMPLATE_REGEX: Regex = "\\$\\{([^}]+)}".toRegex()

        /** Default value for [TemplateTextTransformer]. */
        private const val DEFAULT_VALUE = "No content available."
    }

    /**
     * Creates a new [Instance] instance from this [ContentSamplingTransformer].
     *
     * @param name the name of the [ContentSamplingTransformer.Instance]
     * @param inputs Map of named input [Operator]s
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
        require(inputs.size == 1)  { "The ${this::class.simpleName} only supports one input operator. If you want to combine multiple inputs, use explicit merge strategies." }
        val template = context[name, "template"] ?: throw IllegalArgumentException("The template text transformer requires a template.")
        val defaultValue = context[name, "defaultValue"] ?: DEFAULT_VALUE
        val contentFields = TEMPLATE_REGEX.findAll(template).map { it.groupValues[1] }.toList()
        return Instance(name, inputs.values.first(), context.contentFactory,template, contentFields, defaultValue)
    }

    private class Instance(
        override val name: String,
        override val input: Operator<out Retrievable>,
        val contentFactory: ContentFactory,
        val template: String,
        val contentFields: List<String>,
        val defaultValue: String,
    ) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            input.toFlow(scope).collect { retrievable: Retrievable ->
                var mergedContent = template

                contentFields.forEach { fieldName ->
                    val placeholder = "\${${fieldName}}"
                    val contentIds = retrievable.content.filterIsInstance<TextContent>().map { it.id }

                    val fieldContent = StringBuilder()
                    contentIds.forEach { id ->
                        retrievable.content.find { it.id == id && it.type == ContentType.TEXT }?.content?.let {
                            fieldContent.append(it)
                        }
                    }

                    val finalContent = if (fieldContent.isEmpty()) defaultValue else fieldContent.toString()
                    mergedContent = mergedContent.replace(placeholder, finalContent)
                }

                if (mergedContent.isNotBlank()) {
                    val content = contentFactory.newTextContent(mergedContent.trim())
                    logger.debug { "Contents from retrievable ${retrievable.id} have been merged into a single content element using template." }
                    emit(retrievable.copy(content = retrievable.content + content, attributes = retrievable.attributes))
                } else {
                    emit(retrievable)
                }
            }
        }
    }
}