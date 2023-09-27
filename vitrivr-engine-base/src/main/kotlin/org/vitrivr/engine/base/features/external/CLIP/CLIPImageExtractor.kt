package org.vitrivr.engine.base.features.external.CLIP

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.base.features.external.ExternalImageExtractor
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO


class CLIPImageExtractor(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    override val input: Operator<Ingested>,
    override val persisting: Boolean = true,
) : ExternalImageExtractor() {

    val QUERY = "/retrieve/clip/text/"


    // External call API
    override fun toFlow(scope: CoroutineScope): Flow<Ingested> {
        val writer = if (this.persisting) {
            this.field.getWriter()
        } else {
            null
        }
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent) {
                val content = retrievable.content.filterIsInstance<ImageContent>()
                // TODO use analyse function to avoid duplicated code!!!

                //val descriptors = this.field.analyser.analyse(content)

                val path = "placeholder.jpg"
                val out = File(path)
                ImageIO.write(content[0].getContent(), "jpg", out)


                val url = URL(DEFAULT_API_ENDPOINT + QUERY + path)
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "text/html")

                if (conn.getResponseCode() !== 200) {
                    throw RuntimeException(
                        "Failed : HTTP error code : " + conn.getResponseCode()
                    )
                }

                val br = BufferedReader(InputStreamReader(conn.inputStream))
                val l = br.readLine()

                val vector: List<Float> = l.substring(1, l.length - 1).split(", ").map { it.toFloat() }

                val descriptor = FloatVectorDescriptor(
                    retrievableId = retrievable.id, transient = !persisting, vector = vector
                )

                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    retrievable.addDescriptor(descriptor)
                }
                writer?.add(descriptor)
            }
            retrievable
        }
            .flowOn(Dispatchers.IO)
    }


}