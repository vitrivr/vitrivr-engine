package org.vitrivr.engine.module.features.feature.external.implementations.prak

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID


class PRAKExternalRetriever<C : ContentElement<*>>(
    field: Schema.Field<C, TextDescriptor>,
    private val queryDescriptor: TextDescriptor,
    context: Context,
    private val hostname: String,
    private val k: Int,
    private val matchAttributeName: String = "uri"
) : AbstractRetriever<C, TextDescriptor>(
    field,
    SimpleBooleanQuery<Value.Text>(
        value = queryDescriptor.value,
        attributeName = field.fieldName,
        limit = k.toLong()
    ),
    context
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override fun toFlow(scope: CoroutineScope) = flow {
        val queryText = queryDescriptor.value.value

        val requestBody = json.encodeToString(
            PrakQueryRequest(
                k = k,
                queries = queryText
            )
        )

        val client = HttpClient(CIO)

        try {
            val response = client.post("http://acheron.ms.mff.cuni.cz:42032/textQuery/") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw IllegalArgumentException(
                    "PRAK retrieval failed. Status: ${response.status}. Body: $errorBody"
                )
            }

            val responseBody = response.bodyAsText()
            val results = json
                .decodeFromString<List<PrakApiResult>>(responseBody)
                .sortedBy { it.rankInt }


            for (result in results) {
                val attr = setOf(
                    ScoreAttribute.Similarity(result.scoreDouble),
                    PrakResultAttribute(
                        uri = result.uri,
                        rank = result.rankInt,
                        score = result.scoreDouble,
                        id = result.id,
                        labels = result.label,
                        time = result.time
                    )
                )
                val retrieved = Retrieved(
                    id = UUID.randomUUID(),
                    type = "Prak",
                    content = emptyList(),
                    descriptors = emptySet(),
                    attributes = attr,
                    relationships = emptySet(),
                    transient = true
                )
                emit(
                    retrieved
                )
            }

        } finally {
            client.close()
        }
    }
}