package org.vitrivr.engine.server.api.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.json.JsonMapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.vitrivr.engine.core.features.metadata.source.exif.logger
import java.lang.reflect.Type

object KotlinxJsonMapper : JsonMapper {

    private val fallbackMapper = jacksonObjectMapper()

    override fun <T : Any> fromJsonString(json: String, targetType: Type): T {

        return try {
            @Suppress("UNCHECKED_CAST")
            val serializer = serializer(targetType) as KSerializer<T>
            Json.decodeFromString(serializer, json)
        } catch (e: SerializationException) {
            "Error while deserializing JSON: ${e.message}".let {
                logger.error { it }
                throw Exception(it)
            }
            null
        } catch (e: IllegalStateException) {
            "Error state: ${e.message}".let {
                logger.error { it }
                throw Exception(it)
            }
            null
        } ?: fallbackMapper.readValue(json, fallbackMapper.typeFactory.constructType(targetType))

    }

    override fun toJsonString(obj: Any, type: Type): String {

        return try {
            val serializer = serializer(type)
            Json.encodeToString(serializer, obj)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalStateException) {
            null
        } ?: fallbackMapper.writeValueAsString(obj)

    }
}