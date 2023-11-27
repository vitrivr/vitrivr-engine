package org.vitrivr.engine.tools

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.modules.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.serializersModule

import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.FileMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.typeOf


@Serializable
data class CineastMultimediaObject(
        @SerialName("cineast.cineast_multimediaobject.objectid")
        val objectid: String,
        @SerialName("cineast.cineast_multimediaobject.mediatype")
        val mediatype : Int,
        @SerialName("cineast.cineast_multimediaobject.name")
        val name: String,
        @SerialName("cineast.cineast_multimediaobject.path")
        val path: String)

@Serializable
data class CineastSegment(
        @SerialName("cineast.cineast_segment.segmentid")
        val segmentid: String,
        @SerialName("cineast.cineast_segment.objectid")
        val objectid: String,
        @SerialName("cineast.cineast_segment.segmentnumber")
        val segmentnumber: Int,
        @SerialName("cineast.cineast_segment.segmentstart")
        val segmentstart: Int,
        @SerialName("cineast.cineast_segment.segmentend")
        val segmentend: Int,
        @SerialName("cineast.cineast_segment.segmentstartabs")
        val segmentstartabs : Float,
        @SerialName("cineast.cineast_segment.segmentendabs")
        val segmentendabs: Float
)


@Serializable
data class CineastObjectMetadata(val objectid: String, val domain: String, val key: String, val value: String)

@Serializable
data class CineastSegmentMetadata(val segmentid: String, val domain: String, val key: String, val value: String)

interface CineastFeature {

    val id: String
    abstract fun toDescriptor(idmap:Map<String,String>): Descriptor
}


@Serializable
data class CineastVectorFeature(override val id:String, val feature: List<Float>) : CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): FloatVectorDescriptor {
        return FloatVectorDescriptor(
                id = UUID.randomUUID(),
                retrievableId = RetrievableId.fromString(idmap[id])?:throw IllegalArgumentException("Could not find retrievable id for id $id"),
                vector = feature,
                transient = false
        )
    }
}

@Serializable
data class CineastStringFeature(override val id: String, val feature: String): CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): StringDescriptor {
        return StringDescriptor(
                id = UUID.randomUUID(),
                retrievableId = RetrievableId.fromString(idmap[id])?:throw IllegalArgumentException("Could not find retrievable id for id $id"),
                value = feature,
                transient = false
        )
    }
}


@Serializable
data class MigrationConfig(val segmentspath: String, val objectspath: String, val features: List<FeatureConfig>)

@Serializable
data class FeatureConfig(val path: String, val fieldname: String)


fun String.toUUID(): UUID {
    val hash = MessageDigest.getInstance("SHA-256")
            .digest(this.toByteArray())
            .copyOfRange(0, 16) // Take first 128 bits (16 bytes)

    // Set the version to 5 - Named Based UUID
    hash[6] = (hash[6] and 0x0f).toByte() // Clear version
    hash[6] = (hash[6] or 0x50).toByte() // Set to version 5

    // Set the variant to 2 as per RFC 4122
    hash[8] = (hash[8] and 0x3f).toByte() // Clear the variant
    hash[8] = (hash[8] or 0x80.toByte()).toByte() // Set to IETF variant

    val long1 = hash.copyOfRange(0, 8).fold(0L) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff).toLong() }
    val long2 = hash.copyOfRange(8, 16).fold(0L) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff).toLong() }

    return UUID(long1, long2)
}


// Function to obtain a serializer for a given KClass instance at runtime.
@OptIn(InternalSerializationApi::class)
fun <T : Any> serializerForType(type: KClass<T>): KSerializer<T> {
    return type.serializer()
}

// Function to deserialize a JSON string into a list of objects of a dynamic type.
fun <T : Any> decodeJsonListFromString(jsonString: String, type: KClass<T>): List<T> {
    val json = Json { serializersModule = SerializersModule { contextual(type, serializerForType(type)) } }
    val listSerializer = ListSerializer(serializerForType(type))
    return json.decodeFromString(listSerializer, jsonString)
}

class CineastMigrationTool(val migrationconfigpath: String, val schemaconfigpath : String) {

    private val migrationConfig: MigrationConfig
    private val schemaConfig: SchemaConfig
    private val schemaManager: SchemaManager
    val schema: Schema
    val descriptorToFeatureMap: Map<KClass<*>, KClass<out CineastFeature>> = mapOf(
            FloatVectorDescriptor::class to CineastVectorFeature::class,
            StringDescriptor::class to CineastStringFeature::class
    )

    private val idmap = mutableMapOf<String, String>()

    init {
        // Read the configuration strings
        val configstring = Files.readString(Paths.get(migrationconfigpath))
        val schemaconfigstring = Files.readString(Paths.get(schemaconfigpath))

        // Decode the configuration strings into objects
        migrationConfig = Json.decodeFromString(configstring)
        schemaConfig = Json.decodeFromString(schemaconfigstring)

        // Initialize the SchemaManager and load the schema
        schemaManager = SchemaManager()
        schemaManager.load(schemaConfig)
        schema = schemaManager.getSchema(schemaConfig.name)
                ?: throw IllegalArgumentException("Schema ${schemaConfig.name} not found")
    }

    fun getFeatureClassFromDescriptor(descriptorClass: KClass<*>): KClass<out CineastFeature>? {
        return descriptorToFeatureMap[descriptorClass]
    }
    fun migrate() {

        var initialized = 0
        var initializer: Initializer<*> = schema.connection.getRetrievableInitializer()
        if (!initializer.isInitialized()) {
            initializer.initialize()
            initialized += 1
        }
        for (field in schema.fields()) {
            initializer = field.getInitializer()
            if (!initializer.isInitialized()) {
                initializer.initialize()
                initialized += 1
            }
        }
        println("Successfully initialized schema '${schema.name}'; created $initialized entities.")

        val objectsjson = Files.readString(Paths.get(migrationConfig.objectspath))
        val segmentsjson = Files.readString(Paths.get(migrationConfig.segmentspath))

        val objects = Json.decodeFromString(objectsjson) as List<CineastMultimediaObject>
        val segments = Json.decodeFromString(segmentsjson) as List<CineastSegment>

        val filemetadatawriter = schema.get("file")?.getWriter()?: throw IllegalArgumentException("Could not find file metadata writer in schema ${schema.name}")
        val temporalmetadatawriter = schema.get("time")?.getWriter()?: throw IllegalArgumentException("Could not find temporal metadata writer in schema ${schema.name}")

        val retrievableWriter = schema.connection.getRetrievableWriter()

        for (mobject in objects){
            val uuid = mobject.objectid.toUUID()
            idmap[mobject.objectid] = uuid.toString()
            val objectRetrievable = object : Retrievable {
                override val id: RetrievableId = uuid
                override val type = "source"
                override val transient = false
            }
            val size = 0L
            try {
                val size = Files.size(Paths.get(mobject.path))
            } catch (e: Exception) {
                println("Could not find file ${mobject.path}, setting size to 0")
            }
            val fileMetadataDescriptor = FileMetadataDescriptor(
                    id=UUID.randomUUID(),
                    retrievableId = objectRetrievable.id,
                    path=mobject.path,
                    size=size,
            )
            filemetadatawriter.add(fileMetadataDescriptor)
            retrievableWriter.add(objectRetrievable)
        }

        for (segment in segments) {
            val uuid = segment.segmentid.toUUID()
            idmap[segment.segmentid] = uuid.toString()
            val sourceRetrievable = schema.connection.getRetrievableReader().get(RetrievableId.fromString(idmap[segment.objectid])) ?: throw IllegalArgumentException("Could not find source retrievable for object ${segment.objectid}")
            val ingested = object : Retrievable {
                override val id = uuid
                override val type = "segment"
                override val transient = false
        }
            val temporalMetadataDescriptor = TemporalMetadataDescriptor(
                    id = UUID.randomUUID(),
                    retrievableId = ingested.id,
                    startNs = segment.segmentstartabs.toLong() * 1000 * 1000 * 1000,
                    endNs = segment.segmentendabs.toLong() * 1000 * 1000 * 1000,
            )
            temporalmetadatawriter.add(temporalMetadataDescriptor)
            schema.connection.getRetrievableWriter().add(ingested)
            retrievableWriter.connect(ingested.id, "partOf", sourceRetrievable.id)
        }

        for (featureConfig in migrationConfig.features) {
            migrateFeature(featureConfig)
        }

    }

    fun migrateFeature(featureConfig: FeatureConfig) {
        val path = featureConfig.path
        val fieldName = featureConfig.fieldname

        val field = schema.get(name = fieldName) ?: throw IllegalArgumentException("Field $fieldName does not exist in schema ${schema.name}.")

        val descriptorClass = field.analyser.descriptorClass

        val featureClass = getFeatureClassFromDescriptor(descriptorClass) ?: throw IllegalArgumentException("Feature class not found for descriptor class $descriptorClass")

        val jsonString = Files.readString(Paths.get(path))

        @Suppress("UNCHECKED_CAST")
        val features = decodeJsonListFromString(jsonString, featureClass as KClass<Any>) as List<CineastFeature>

        val descriptors = features.map { feature  ->
            if (!featureClass.isInstance(feature)) {
                throw IllegalArgumentException("Feature is not an instance of expected feature class $featureClass")
            }
            feature.toDescriptor(idmap)
        }

        field.getWriter().addAll(descriptors)
    }
}



fun main(args: Array<String>) {
    // Check for the required arguments
    if (args.size < 2) {
        println("Usage: <program> <migrationconfigpath> <schemaconfigpath>")
        return
    }

    // Obtain the paths from the command-line arguments
    val migrationConfigPath = args[0]
    val schemaConfigPath = args[1]

    try {
        // Create an instance of the migration tool with the paths
        val migrationTool = CineastMigrationTool(migrationConfigPath, schemaConfigPath)

        // Perform the migration
        migrationTool.migrate()

        println("Migration completed successfully.")

    } catch (e: Exception) {
        // Catch any exceptions that may occur and print the error message
        println("An error occurred during migration: ${e.message}")
        e.printStackTrace()
    }
}
