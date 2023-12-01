package org.vitrivr.engine.tools

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.FileReader
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.RasterDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.SkeletonDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.FileMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.reflect.KClass


val gson = Gson()

// Define a function to parse a list of objects from a JSON file using streaming
// Define a function to parse a list of objects from a JSON file using streaming
fun <T> parseJsonList(filePath: String, clazz: Class<T>): List<T> {
    val items = mutableListOf<T>()
    FileReader(filePath).use { fileReader ->
        JsonReader(fileReader).use { jsonReader ->
            jsonReader.beginArray() // Start of the JSON array
            while (jsonReader.hasNext()) {
                val rawJson = JsonParser.parseReader(jsonReader).toString()
                //println(rawJson) // Print the raw JSON string
                val item = gson.fromJson<T>(rawJson, clazz)
                items.add(item)
            }
            jsonReader.endArray() // End of the JSON array
        }
    }
    return items
}

fun <T> streamJsonItems(filePath: String, clazz: Class<T>): Sequence<T> = sequence {
    FileReader(filePath).use { fileReader ->
        JsonReader(fileReader).use { jsonReader ->
            jsonReader.beginArray() // Start of the JSON array
            while (jsonReader.hasNext()) {
                val rawJson = JsonParser.parseReader(jsonReader).toString()
                val item = gson.fromJson<T>(rawJson, clazz)
                yield(item) // Yield the item to the sequence
            }
            jsonReader.endArray() // End of the JSON array
        }
    }
}


@Serializable
data class CineastMultimediaObject(
        //@SerialName("cineast.cineast_multimediaobject.objectid")
        val objectid: String,
        //@SerialName("cineast.cineast_multimediaobject.mediatype")
        val mediatype : Int,
        //@SerialName("cineast.cineast_multimediaobject.name")
        val name: String,
        //@SerialName("cineast.cineast_multimediaobject.path")
        val path: String)

@Serializable
data class CineastSegment(
        //@SerialName("cineast.cineast_segment.segmentid")
        val segmentid: String,
        //@SerialName("cineast.cineast_segment.objectid")
        val objectid: String,
        //@SerialName("cineast.cineast_segment.segmentnumber")
        val segmentnumber: Int,
        //@SerialName("cineast.cineast_segment.segmentstart")
        val segmentstart: Int,
        //@SerialName("cineast.cineast_segment.segmentend")
        val segmentend: Int,
        //@SerialName("cineast.cineast_segment.segmentstartabs")
        val segmentstartabs : Float,
        //@SerialName("cineast.cineast_segment.segmentendabs")
        val segmentendabs: Float
)


@Serializable
data class CineastObjectMetadata(
        //@SerialName("cineast.cineast_metadata.objectid")
        val objectid: String,
        //@SerialName("cineast.cineast_metadata.domain")
        val domain: String,
        //@SerialName("cineast.cineast_metadata.key")
        val key: String,
        //@SerialName("cineast.cineast_metadata.value")
        val value: String)

@Serializable
data class CineastSegmentMetadata(
        //@SerialName("cineast.cineast_segmentmetadata.segmentid")
        val segmentid: String,
        //@SerialName("cineast.cineast_segmentmetadata.domain")
        val domain: String,
        //@SerialName("cineast.cineast_segmentmetadata.key")
        val key: String,
        //@SerialName("cineast.cineast_segmentmetadata.value")
        val value: String)

interface CineastFeature {

    val id: String
    abstract fun toDescriptor(idmap:Map<String,String>): Descriptor?
}


@Serializable
data class CineastVectorFeature(override val id:String, val feature: List<Float>) : CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): FloatVectorDescriptor? {
        val id = this.id
        if(idmap[id] == null){
            return null
        }
        return FloatVectorDescriptor(
                id = DescriptorId.randomUUID(),
                retrievableId = RetrievableId.fromString(idmap[id]),
                vector = feature,
                transient = false
        )
    }
}

@Serializable
data class CineastStringFeature(override val id: String, val feature: String): CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): StringDescriptor? {
        val id = this.id
        if(idmap[id] == null){
            return null
        }
        return StringDescriptor(
                id = DescriptorId.randomUUID(),
                retrievableId = RetrievableId.fromString(idmap[id])?:throw IllegalArgumentException("Could not find retrievable id for id $id"),
                value = feature,
                transient = false
        )
    }
}

@Serializable
data class CineastSkeletonPoseFeature(override val id: String, val person: Int, val skeleton: List<Float>, val weights: List<Float>): CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): SkeletonDescriptor? {
        val id = this.id
        if(idmap[id] == null){
            return null
        }
        return SkeletonDescriptor(
                id = DescriptorId.randomUUID(),
                retrievableId = RetrievableId.fromString(idmap[id]),
                person = person,
                skeleton = skeleton,
                weights = weights,
                transient = false)
    }
}

@Serializable
data class CineastRasterFeature(override val id: String, val hist: List<Float>, val raster: List<Float>): CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): RasterDescriptor? {
        val id = this.id
        if (idmap[id] == null) {
            return null
        }
        return RasterDescriptor(id = DescriptorId.randomUUID(), retrievableId = RetrievableId.fromString(idmap[id]), hist = hist, raster = raster)
    }
}

@Serializable
data class MigrationConfig(
        val segmentspath: String,
        val objectspath: String,
        val segmentmetadatapath: String,
        val objectmetadatapath: String,
        val features: List<FeatureConfig>
)

@Serializable
data class FeatureConfig(val path: String, val fieldname: String)


//fun String.toUUID(): UUID {
//    val hash = MessageDigest.getInstance("SHA-256")
//            .digest(this.toByteArray())
//            .copyOfRange(0, 16) // Take first 128 bits (16 bytes)
//
//    // Set the version to 5 - Named Based UUID
//    hash[6] = (hash[6] and 0x0f).toByte() // Clear version
//    hash[6] = (hash[6] or 0x50).toByte() // Set to version 5
//
//    // Set the variant to 2 as per RFC 4122
//    hash[8] = (hash[8] and 0x3f).toByte() // Clear the variant
//    hash[8] = (hash[8] or 0x80.toByte()).toByte() // Set to IETF variant
//
//    val long1 = hash.copyOfRange(0, 8).fold(0L) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff).toLong() }
//    val long2 = hash.copyOfRange(8, 16).fold(0L) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff).toLong() }
//
//    return UUID(long1, long2)
//}


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
            StringDescriptor::class to CineastStringFeature::class,
            SkeletonDescriptor::class to CineastSkeletonPoseFeature::class,
            RasterDescriptor::class to CineastRasterFeature::class,
    )

    private var idmap: MutableMap<String, String> = mutableMapOf<String, String>()

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

    fun initialize() {
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

    }

    fun migrate_objects() {

        val objects: List<CineastMultimediaObject> = parseJsonList(migrationConfig.objectspath, CineastMultimediaObject::class.java)
        val retrievableWriter = schema.connection.getRetrievableWriter()
        val filemetadatawriter = schema.get("file")?.getWriter() ?: throw IllegalArgumentException("Could not find file metadata writer in schema ${schema.name}")

        //val retr_list = mutableListOf<Retrievable>()
        //val desc_list = mutableListOf<FileMetadataDescriptor>()

        //var i = 0
        for (mobject in objects) {
            val uuid = RetrievableId.randomUUID()
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
                    id = DescriptorId.randomUUID(),
                    retrievableId = objectRetrievable.id,
                    path = mobject.path,
                    size = size,
            )
            filemetadatawriter.add(fileMetadataDescriptor)
            retrievableWriter.add(objectRetrievable)
        }

    }

    fun save_idmap() {
        val idmapjson = Json.encodeToString(idmap as Map<String, String>)
        Files.writeString(Paths.get("idmap.json"), idmapjson)
    }

    fun load_idmap() {
        val idmapjson = Files.readString(Paths.get("idmap.json"))
        idmap = Json.decodeFromString(idmapjson)
    }

    fun migrate_objectmetadata() {
        val widths = mutableMapOf<String, Int>()
        val heights = mutableMapOf<String, Int>()

        val objectmetadata: List<CineastObjectMetadata> = parseJsonList(migrationConfig.objectmetadatapath, CineastObjectMetadata::class.java)
        val mediadimensionswriter = schema.get("dimensions")?.getWriter() ?: throw IllegalArgumentException("Could not find media dimensions writer in schema ${schema.name}")
        val videofpswriter = schema.get("fps")?.getWriter() ?: throw IllegalArgumentException("Could not find video fps writer in schema ${schema.name}")
        val videodurationwriter = schema.get("duration")?.getWriter() ?: throw IllegalArgumentException("Could not find video duration writer in schema ${schema.name}")



        for (mobjectmetadata in objectmetadata) {
            val retrievableId = idmap[mobjectmetadata.objectid] ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}")

            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "width") {
                widths[retrievableId] = mobjectmetadata.value.toInt()
            }
            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "height") {
                val height = mobjectmetadata.value.toInt()
                heights[retrievableId] = height

                if (widths.containsKey(retrievableId)) {
                    val width = widths[retrievableId] ?: throw IllegalArgumentException("Could not find width for object ${mobjectmetadata.objectid}")
                    val dimensionsDescriptor = MediaDimensionsDescriptor(
                            id = DescriptorId.randomUUID(),
                            retrievableId = RetrievableId.fromString(retrievableId) ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}"),
                            width = width,
                            height = height,
                            transient = false
                    )
                    mediadimensionswriter.add(dimensionsDescriptor)
                }
            }
            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "fps") {
                val fps = mobjectmetadata.value.toFloat()
                val retrievableId = idmap[mobjectmetadata.objectid] ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}")
                val fpsDescriptor = FloatDescriptor(
                        id = DescriptorId.randomUUID(),
                        retrievableId = RetrievableId.fromString(retrievableId) ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}"),
                        value = fps,
                        transient = false
                )
                videofpswriter.add(fpsDescriptor)
            }

            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "duration") {
                val retrievableId = idmap[mobjectmetadata.objectid] ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}")
                val duration = mobjectmetadata.value.toFloat()
                val durationDescriptor = FloatDescriptor(
                        id = DescriptorId.randomUUID(),
                        retrievableId = RetrievableId.fromString(retrievableId) ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}"),
                        value = duration,
                        transient = false
                )
                videodurationwriter.add(durationDescriptor)
            }


        }
    }

    fun migrate_segments() {
        val segments: List<CineastSegment> = parseJsonList(migrationConfig.segmentspath, CineastSegment::class.java)
        val temporalmetadatawriter = schema.get("time")?.getWriter() ?: throw IllegalArgumentException("Could not find temporal metadata writer in schema ${schema.name}")
        val retrievableWriter = schema.connection.getRetrievableWriter()


        val batchSize = 5000
        val ingestedList = mutableListOf<Retrievable>()
        val temporalMetadataList = mutableListOf<TemporalMetadataDescriptor>()
        val parentIdList = mutableListOf<String>()

        for ((index, segment) in segments.withIndex()) {
            val uuid = RetrievableId.randomUUID()
            idmap[segment.segmentid] = uuid.toString()

            val ingested = object : Retrievable {
                override val id = uuid
                override val type = "segment"
                override val transient = false
            }

            val temporalMetadataDescriptor = TemporalMetadataDescriptor(
                    id = DescriptorId.randomUUID(),
                    retrievableId = ingested.id,
                    startNs = segment.segmentstartabs.toLong() * 1000 * 1000 * 1000,
                    endNs = segment.segmentendabs.toLong() * 1000 * 1000 * 1000,
            )

            ingestedList.add(ingested)
            temporalMetadataList.add(temporalMetadataDescriptor)
            parentIdList.add(idmap[segment.objectid] ?: throw IllegalArgumentException("Could not find retrievable id for segment ${segment.objectid}"))


            // Check if the batch size is reached
            if ((index + 1) % batchSize == 0 || index == segments.size - 1) {
                retrievableWriter.addAll(ingestedList)
                retrievableWriter.connectAll(ingestedList.map { it.id }, "isPartOf", parentIdList.map { RetrievableId.fromString(it) ?: throw IllegalArgumentException("Could not find retrievable id for segment $it") })
                temporalmetadatawriter.addAll(temporalMetadataList)

                // Clear the lists for the next batch
                ingestedList.clear()
                temporalMetadataList.clear()
                parentIdList.clear()
            }
        }


    }


    fun migrate_segmentmetadata() {
        val segmentmetadata: List<CineastSegmentMetadata> = parseJsonList(migrationConfig.segmentmetadatapath, CineastSegmentMetadata::class.java)
        val dominantcolorwriter = schema.get("dominantcolor")?.getWriter() ?: throw IllegalArgumentException("Could not find dominant color writer in schema ${schema.name}")

        val dominantcolordescriptors = mutableListOf<StringDescriptor>()
        for (msegmentmetadata in segmentmetadata) {
            if (msegmentmetadata.domain == "dominantcolor" && msegmentmetadata.key == "color") {
                val color = msegmentmetadata.value
                try {
                    val retrievableId = RetrievableId.fromString(idmap[msegmentmetadata.segmentid]) ?: throw IllegalArgumentException("Could not find retrievable id for segment ${msegmentmetadata.segmentid}")
                    val dominantColorDescriptor = StringDescriptor(
                            id = DescriptorId.randomUUID(),
                            retrievableId = retrievableId,
                            value = color,
                            transient = false
                    )
                    dominantcolordescriptors.add(dominantColorDescriptor)

                } catch (e: Exception) {
                    println("Could not find retrievable id for segment ${msegmentmetadata.segmentid}")
                }


            }
        }
        dominantcolorwriter.addAll(dominantcolordescriptors)
    }

    fun migrateAllFeatures() = runBlocking {
        migrationConfig.features.map { featureConfig ->
            launch(Dispatchers.Default) { // Use the appropriate dispatcher
                migrateFeature(featureConfig.fieldname)
            }
        }.joinAll() // Wait for all tasks to complete
    }

    suspend fun migrateFeature(fieldName: String) = withContext(Dispatchers.Default) {

        println("Migrating feature $fieldName")
        val featureConfig = migrationConfig.features.find { it.fieldname == fieldName } ?: throw IllegalArgumentException("Feature config for field $fieldName not found.")
        val path = featureConfig.path

        val field = schema.get(name = fieldName) ?: throw IllegalArgumentException("Field $fieldName does not exist in schema ${schema.name}.")

        val descriptorClass = field.analyser.descriptorClass

        val featureClass = getFeatureClassFromDescriptor(descriptorClass) ?: throw IllegalArgumentException("Feature class not found for descriptor class $descriptorClass")

        val featureStream = streamJsonItems(path, featureClass.java)

        val fieldwriter = field.getWriter()

        val fieldreader = field.getReader()

        if(fieldreader.count() > 0) {
            println("Field $fieldName already contains ${fieldreader.count()} entries, skipping migration.")
            return@withContext
        }

        File("$fieldName-skipped_ids.json").bufferedWriter().use { writer ->
            writer.write("[") // Start of JSON array
            var firstEntry = true

            featureStream.chunked(1000).forEach { batch ->
                val descriptors: Iterable<Descriptor> = batch.mapNotNull { feature ->
                    if (!featureClass.isInstance(feature)) {
                        throw IllegalArgumentException("Feature is not an instance of expected feature class $featureClass")
                    }
                    val descriptor = feature.toDescriptor(idmap)
                    if (descriptor == null) {
                        if (firstEntry) {
                            firstEntry = false
                        } else {
                            writer.write(",") // Add comma before next entry, except for the first
                        }
                        writer.write(Gson().toJson(feature.id))
                        //println("Could not find retrievable id for feature ${feature.id}")
                    }
                    descriptor
                }
                fieldwriter.addAll(descriptors)
            }

            writer.write("]") // End of JSON array
        }

        println("Finished migrating feature $fieldName")
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

        // Create an instance of the migration tool with the paths
        val migrationTool = CineastMigrationTool(migrationConfigPath, schemaConfigPath)

        // Perform the migration
//        migrationTool.initialize()
//        migrationTool.migrate_objects()
//        migrationTool.save_idmap()
//
//        migrationTool.migrate_objectmetadata()

        // migrationTool.migrate_segments()
        //migrationTool.migrate_segmentmetadata()
        migrationTool.initialize()
        migrationTool.load_idmap()

        runBlocking {
            migrationTool.migrateFeature("skeletonpose")
        }



        println("Migration completed successfully.")


    }


