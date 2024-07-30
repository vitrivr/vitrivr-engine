package org.vitrivr.engine.server.api.rest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.json.JsonMapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.io.InputStream
import java.lang.reflect.Type

/**
 * A [JsonMapper] implementation for Javalin that uses Kotlinx Serialization framework.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
object KotlinxJsonMapper : JsonMapper {

    /** The [SerializersModule] used by this [KotlinxJsonMapper]. */
    private val projectModule = SerializersModule {}

    /** The [Json] object to perform de-/serialization with.  */
    private val json = Json { serializersModule = projectModule }

    /** The [KLogger] instance used by this [KotlinxJsonMapper]. */
    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * Converts a JSON [String] to an object representation using Kotlinx serialization framework.
     *
     * @param json The [String] to parse.
     * @param targetType The target [Type]
     * @return Object [T]
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> fromJsonString(json: String, targetType: Type): T {
        val deserializer = this.projectModule.serializer(targetType)
        return this.json.decodeFromString(deserializer, json) as T
    }

    /**
     * Converts an object [Any] to a JSON string using Kotlinx serialization. Javalin uses this method for
     * io.javalin.http.Context.json(Object), as well as the CookieStore class, WebSockets messaging, and JavalinVue.
     *
     * @param obj The object [Any] to serialize.
     * @param type The target [Type]
     * @return JSON string representation.
     */
    override fun toJsonString(obj: Any, type: Type): String {
        return this.json.encodeToString(this.projectModule.serializer(type), obj)
    }

    /**
     * Converts a JSON [InputStream] to an object representation using Kotlinx serialization framework.
     *
     * @param json The [InputStream] to parse.
     * @param targetType The target [Type]
     * @return Object [T]
     */
    override fun <T : Any> fromJsonStream(json: InputStream, targetType: Type): T {
        val deserializer = this.projectModule.serializer(targetType)
        return this.json.decodeFromStream(deserializer, json) as T
    }
}