package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

private val logger = KotlinLogging.logger {}

class ContentMergingTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        val contentFields = context[name, "contentFields"]?.split(",") ?: throw IllegalArgumentException("The content merging transformer requires a list of content fields.")
        return Instance(
            input = input,
            contentFactory = (context as IndexContext).contentFactory,
            contentFields = contentFields,
            name = name
        )
    }

    private class Instance(
        override val input: Operator<out Retrievable>,
        val contentFactory: ContentFactory,
        val contentFields: List<String>,
        val name: String
    ) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
            input.toFlow(scope).collect { retrievable: Retrievable ->
                val mergedContent = StringBuilder()
                contentFields.forEach { fieldName ->
                    retrievable.getContent(fieldName)?.let { content ->
                        mergedContent.append(content)
                        mergedContent.append("\n")
                    }
                }
                if (mergedContent.isNotEmpty()) {
                    val content = contentFactory.newTextContent(mergedContent.toString().trim())
                    retrievable.addContent(content)
                    retrievable.addAttribute(ContentAuthorAttribute(content.id, name))
                    logger.debug { "Contents from fields $contentFields of retrievable ${retrievable.id} have been merged into a single content element." }
                }
                emit(retrievable)
            }
        }
    }
}