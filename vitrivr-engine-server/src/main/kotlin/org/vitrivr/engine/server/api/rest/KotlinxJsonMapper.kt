    import io.javalin.json.JsonMapper
    import kotlinx.serialization.*
    import kotlinx.serialization.json.Json
    import kotlinx.serialization.serializer
    import kotlin.reflect.KType
    import java.lang.reflect.ParameterizedType
    import java.lang.reflect.Type
    import kotlin.reflect.KTypeProjection
    import kotlin.reflect.full.createType

    object KotlinxJsonMapper : JsonMapper {

        private val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        override fun <T : Any> fromJsonString(jsonString: String, targetType: Type): T {
            return try {
                val kType : KType = targetType.toKType()
                val serializer = json.serializersModule.serializer(kType)
                json.decodeFromString(serializer, jsonString) as T
            } catch (e: SerializationException) {
                throw Exception("Error while deserializing JSON: ${e.message}")
            } catch (e: IllegalStateException) {
                throw Exception("Error state: ${e.message}")
            }
        }

        override fun toJsonString(obj: Any, type: Type): String {
            return try {
                val kType : KType = type.toKType()
                val serializer = json.serializersModule.serializer(kType)
                json.encodeToString(serializer, obj)
            } catch (e: SerializationException) {
                throw Exception("Error while serializing JSON: ${e.message}")
            } catch (e: IllegalStateException) {
                throw Exception("Error state: ${e.message}")
            }
        }

        private fun Type.toKType(): KType {
            return when (this) {
                is ParameterizedType -> {
                    val rawType = (this.rawType as Class<*>).kotlin
                    val args = this.actualTypeArguments.map { it.toKType() }
                    rawType.createType(args.map { KTypeProjection.invariant(it) })
                }
                is Class<*> -> this.kotlin.createType()
                else -> throw IllegalArgumentException("Unsupported Type: $this")
            }
        }
    }
