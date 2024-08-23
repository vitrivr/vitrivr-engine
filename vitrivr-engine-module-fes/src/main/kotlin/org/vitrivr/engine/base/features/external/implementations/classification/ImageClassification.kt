package org.vitrivr.engine.base.features.external.implementations.classification

import org.vitrivr.engine.base.features.external.api.AbstractApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser.Companion.merge
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*


/**
 * Implementation of the [ImageClassification] [ExternalFesAnalyser] that uses the [AbstractApi] to classify images.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class ImageClassification : ExternalFesAnalyser<ContentElement<*>, LabelDescriptor>() {
    companion object{
        const val CLASSES_PARAMETER_NAME = "classes"
        const val THRESHOLD_PARAMETER_NAME = "threshold"
        const val TOPK_PARAMETER_NAME = "top_k"
    }

    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = LabelDescriptor::class

    /**
     * Generates a prototypical [LabelDescriptor] for this [ImageClassification].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [LabelDescriptor]
     */
    override fun prototype(field: Schema.Field<*,*>): LabelDescriptor {
        return LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), mapOf("label" to Value.String(""), "confidence" to Value.Float(0.0f)))
    }

    /**
     * Generates and returns a new [ImageClassification] instance for this [ImageClassification].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [ImageClassification].
     * @param context The [IndexContext] to use with the [ImageClassification].
     * @return [ImageClassification]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = ImageClassificationExtractor(input, null, this,context.local[name] ?: emptyMap())

    /**
     * Generates and returns a new [ImageClassification] instance for this [ImageClassification].
     *
     * @param field The [Schema.Field] to create an [ImageClassification] for.
     * @param input The [Operator] that acts as input to the new [ImageClassification].
     * @param context The [IndexContext] to use with the [ImageClassification].
     * @return [ImageClassification]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, LabelDescriptor>, input: Operator<Retrievable>, context: IndexContext) = ImageClassificationExtractor(input, field, this, merge(field, context))

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, LabelDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, LabelDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleBooleanQuery<*>) { "The query is not a boolean query. This is a programmer's error!" }
        return object : AbstractRetriever<ContentElement<*>, LabelDescriptor>(field, query, context){}
    }

    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, LabelDescriptor>,
        content: Collection<ContentElement<*>>,
        context: QueryContext
    ): Retriever<ContentElement<*>, LabelDescriptor> {
        val firstContent = content.first()
        if (content.size != 1 || firstContent !is TextContent) {
            throw IllegalArgumentException("The content does not match the expected type. This is a programmer's error!")
        }
        val query = SimpleBooleanQuery(value = Value.String(firstContent.content), attributeName = "label")
        return newRetrieverForQuery(field, query, context)

    }
}