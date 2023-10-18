package org.vitrivr.engine.base.features.external.implementations.CLIP_Image

import com.google.gson.Gson
import org.vitrivr.engine.base.features.external.ExternalExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO

/**
 * [CLIPImageExtractor] implementation of an [ExternalExtractor] for [CLIPImageFactory].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 * @param host The host address of the external feature API.
 * @param port The port number of the external feature API.
 * @param featureName The name of the feature provided by the external API.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPImageExtractor(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    override val input: Operator<Ingested>,
    override val persisting: Boolean = true,
    override val host: String,
    override val port: Int,
    override val featureName: String,
) : ExternalExtractor<ImageContent, FloatVectorDescriptor>() {

    /**
     * Creates a descriptor for a given retrievable ID and content elements.
     *
     * @param retrievableId The retrievable ID.
     * @param content The list of content elements.
     * @return The created FloatVectorDescriptor.
     */
    override fun createDescriptor(
        retrievable: RetrievableWithContent
    ): FloatVectorDescriptor {
        val list: List<Float> = queryExternalFeatureAPI(retrievable)
        //print(list)
        return FloatVectorDescriptor(
            retrievableId = retrievable.id, transient = !persisting, vector = list
        )
    }

    /**
     * Queries the external feature API for the feature of the given content element.
     *
     * @param content The content element to send to the external feature API.
     * @return The List<Float> representing the obtained external feature.
     */
    override fun queryExternalFeatureAPI(retrievable: RetrievableWithContent): List<Float> {
        // Extract and parse the response from the external feature API
        return createHttpRequest(retrievable.content.filterIsInstance<ImageContent>().first())
    }

    /**
     * Creates an HTTP request to query the external feature API and parses the response as a List<Float>.
     *
     * @param content The content element containing the image.
     * @return The List<Float> representing the external feature.
     */
    override fun createHttpRequest(content: ContentElement<*>): List<Float> {
        val imgContent = content as ImageContent
        val url = "http://$host:$port$featureName"
        val base64 = encodeImageToBase64(imgContent.getContent())
        val body = "data:image/png;base64,$base64}"

        // Create an HttpURLConnection
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Construct the request body
            val requestBody = "data=${URLEncoder.encode("image/png;base64,$base64", StandardCharsets.UTF_8.toString())}"

            // Write the request body to the output stream
            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write("data=$requestBody")
            writer.flush()
            writer.close()
            outputStream.close()

            // Get the response code (optional, but useful for error handling)
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            // Read the response as a JSON string
            val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                val response = inputStream.readLine()
                inputStream.close()
                response
            } else {
                null
            }

            // Parse the JSON string to List<Float> using Gson
            return if (responseJson != null) {
                try {
                    val gson = Gson()
                    gson.fromJson(responseJson, List::class.java) as List<Float>
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // TODO Handle exceptions as needed
        } finally {
            connection.disconnect()
        }
        return emptyList()
    }

    /**
     * Encodes a BufferedImage to base64.
     *
     * @param bufferedImage The BufferedImage to encode.
     * @return The base64-encoded string.
     */
    private fun encodeImageToBase64(bufferedImage: BufferedImage): String {
        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            // Write the BufferedImage to the output stream
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream)

            // Convert the output stream to a byte array
            val imageBytes = byteArrayOutputStream.toByteArray()

            // Encode the byte array to base64
            return Base64.getEncoder().encodeToString(imageBytes)
        } catch (e: IOException) {
            e.printStackTrace()
            // TODO Handle the exception as needed
            return ""
        } finally {
            try {
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
