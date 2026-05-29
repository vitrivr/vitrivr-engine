package org.vitrivr.engine.module.features.feature.external.implementations.prak

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.BoundedCorrespondence
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.IdContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking

import io.ktor.client.request.accept

import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.cio.parseHttpBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor

/**
 * Implementation of the [PRAK] [ExternalAnalyser], which derives the PRAK feature from an [ImageContent] or [TextContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.3.0
 */
class PRAK : ExternalAnalyser<ContentElement<*>, TextDescriptor>() {

    companion object {
        /**
         * Requests the PRAK feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the PRAK feature descriptor.
         * @param hostname The hostname of the external feature descriptor service.
         * @return A list of PRAK feature descriptors.
         */
        private val json = Json {
            encodeDefaults = true
        }

        fun analyse(content: ContentElement<*>, hostname: String): TextDescriptor {
            val query = when (content) {
                is TextContent -> content.text
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }

            return TextDescriptor(
                UUID.randomUUID(),
                null,
                Value.Text(query)
            )
        }
    }


    override val contentClasses = setOf(TextContent::class)
    override val descriptorClass = TextDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [PRAK].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) =
        TextDescriptor(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Value.Text("")
        )

    /**
     * Generates and returns a new [Extractor] instance for this [PRAK].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [Context] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [PRAK]
     * @throws [UnsupportedOperationException], if this [PRAK] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        input: Operator<out Retrievable>,
        context: Context
    ): PRAKExtractor {
        val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return PRAKExtractor(input, this, field, host)
    }

    /**
     * Generates and returns a new [Extractor] instance for this [PRAK].
     *
     * @param name The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [Context] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [PRAK]
     * @throws [UnsupportedOperationException], if this [PRAK] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        name: String,
        input: Operator<out Retrievable>,
        context: Context
    ): PRAKExtractor {
        val host: String = context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        return PRAKExtractor(input, this, name, host)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [PRAK].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [PRAK]
     * @throws [UnsupportedOperationException], if this [PRAK] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        query: Query,
        context: Context
    ): PRAKExternalRetriever<ContentElement<*>> {
        val queryText = when (query) {
            is SimpleBooleanQuery<*> -> {
                when (val value = query.value) {
                    is Value.Text -> value.value
                    is Value.String -> value.value
                    else -> throw IllegalArgumentException(
                        "PRAK only supports textual query values, but got '${value::class.simpleName}'."
                    )
                }
            }

            else -> throw IllegalArgumentException(
                "PRAK expects a SimpleBooleanQuery<Value.Text> or SimpleBooleanQuery<Value.String>, " +
                        "but got '${query::class.simpleName}'."
            )
        }

        val descriptor = TextDescriptor(
            UUID.randomUUID(),
            null,
            Value.Text(queryText)
        )

        val ret=  this.newRetrieverForDescriptors(
            field = field,
            descriptors = listOf(descriptor),
            context = context
        )
        return ret
    }

    /**
     * Generates and returns a new [Retriever] instance for this [PRAK].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [PRAK]
     * @throws [UnsupportedOperationException], if this [PRAK] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        content: Map<String, ContentElement<*>>,
        context: Context
    ): PRAKExternalRetriever<ContentElement<*>> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

        val textContent = content.values
            .filterIsInstance<TextContent>()
            .firstOrNull()
            ?: throw IllegalArgumentException(
                "PRAK external retrieval requires TextContent, but got: " +
                        content.values.joinToString { it::class.simpleName ?: it.toString() }
            )

        val descriptor = analyse(textContent, host)

         val ret = this.newRetrieverForDescriptors(
            field = field,
            descriptors = listOf(descriptor),
            context = context
        )
        return ret
    }

    /**
     * Generates and returns a new [Retriever] instance for this [PRAK].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [FloatVectorDescriptor] elements to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        descriptors: Collection<TextDescriptor>,
        context: Context
    ): PRAKExternalRetriever<ContentElement<*>> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val k = context.getProperty(field.fieldName, "limit")?.toIntOrNull() ?: 100
        val matchAttribute = context.getProperty(field.fieldName, "matchAttribute") ?: "uri"

        return PRAKExternalRetriever(
            field = field,
            queryDescriptor = descriptors.first(),
            context = context,
            hostname = host,
            k = k,
            matchAttributeName = matchAttribute,
            schema=field.schema.name
        )
    }
}