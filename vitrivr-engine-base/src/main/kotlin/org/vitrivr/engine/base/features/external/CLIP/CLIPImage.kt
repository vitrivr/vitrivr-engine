package org.vitrivr.engine.base.features.external.CLIP

import org.vitrivr.engine.base.features.external.ExternalImageFeature
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.model.util.toDescriptorList
import org.vitrivr.engine.core.operators.Operator
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.reflect.KClass

class CLIPImage(
    override val analyserName: String = "CLIPImage",
    override val contentClass: KClass<ImageContent>,
    override val descriptorClass: KClass<FloatVectorDescriptor>
) : ExternalImageFeature() {

    val QUERY = "/retrieve/clip/text/"


    /**
     * Performs the [CLIPImage] analysis on the provided [List] of [ImageContent] elements.
     *
     * @param content The [List] of [ImageContent] elements.
     * @return [List] of [FloatVectorDescriptor]s.
     */
    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> = content.map {

        /* Generate descriptor. */

        //TODO request to get feature vector
        val path = "placeholder.jpg"
        val out = File(path)
        ImageIO.write(it.getContent(), "jpg", out)

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


        FloatVectorDescriptor(UUID.randomUUID(), null, vector, true)
    }.toDescriptorList()


    override fun newExtractor(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Ingested>, persisting: Boolean
    ): CLIPImageExtractor {
        require(field.analyser == this) { "" }
        return CLIPImageExtractor(field, input, persisting)
    }

}