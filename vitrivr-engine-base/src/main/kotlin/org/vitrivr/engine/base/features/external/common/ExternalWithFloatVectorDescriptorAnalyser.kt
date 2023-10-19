package org.vitrivr.engine.base.features.external.common

import com.google.gson.Gson
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.model.util.toDescriptorList
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

abstract class ExternalWithFloatVectorDescriptorAnalyser<C : ContentElement<*>> :
    ExternalAnalyser<C, FloatVectorDescriptor>() {

    abstract val size: Int
    val featureList = List(size) { 0.0f }
    fun executeApiRequest(url: String, requestBody: String): List<Float> {
        // Create an HttpURLConnection
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

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

    abstract override fun requestDescriptor(content: ContentElement<*>): List<Float>

    fun processContent(content: Collection<*>): DescriptorList<FloatVectorDescriptor> {
        val resultList = mutableListOf<FloatVectorDescriptor>()

        for (item in content) {
            val featureVector = requestDescriptor(item as ContentElement<*>)

            val descriptor = FloatVectorDescriptor(
                UUID.randomUUID(), null, featureVector, true
            )

            resultList.add(descriptor)
        }

        return resultList.toDescriptorList()
    }

}