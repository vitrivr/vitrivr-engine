package org.vitrivr.engine.tools

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.vitrivr.engine.core.config.schema.SchemaConfig
import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.RasterDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.SkeletonDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

@Serializable
data class CineastMultimediaObject(
    val objectid: String,
    val mediatype: Int,
    val name: String,
    val path: String
)

@Serializable
data class CineastSegment(
    val segmentid: String,
    val objectid: String,
    val segmentnumber: Int,
    val segmentstart: Int,
    val segmentend: Int,
    val segmentstartabs: Float,
    val segmentendabs: Float
)

@Serializable
data class CineastObjectMetadata(
    val objectid: String,
    val domain: String,
    val key: String,
    val value: String
)

@Serializable
data class CineastSegmentMetadata(
    val segmentid: String,
    val domain: String,
    val key: String,
    val value: String
)

interface CineastFeature {
    val id: String
    fun toDescriptor(idmap: Map<String, String>): Descriptor?
}

@Serializable
data class CineastVectorFeature(override val id: String, val feature: List<Float>) : CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): FloatVectorDescriptor? {
        val id = this.id
        if (idmap[id] == null) {
            return null
        }
        return FloatVectorDescriptor(
            id = DescriptorId.randomUUID(),
            retrievableId = RetrievableId.fromString(idmap[id]),
            vector = feature.map { Value.Float(it) }
        )
    }
}

@Serializable
data class CineastStringFeature(override val id: String, val feature: String) : CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): StringDescriptor? {
        val id = this.id
        if (idmap[id] == null) {
            return null
        }
        return StringDescriptor(
            id = DescriptorId.randomUUID(),
            retrievableId = RetrievableId.fromString(idmap[id]),
            value = Value.String(feature)
        )
    }
}

@Serializable
data class CineastSkeletonPoseFeature(
    override val id: String,
    val person: Int,
    val skeleton: List<Float>,
    val weights: List<Float>
) : CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): SkeletonDescriptor? {
        val id = this.id
        if (idmap[id] == null) {
            return null
        }
        return SkeletonDescriptor(
            id = DescriptorId.randomUUID(),
            retrievableId = RetrievableId.fromString(idmap[id]),
            person = Value.Int(person),
            skeleton = skeleton.map { Value.Float(it) },
            weights = weights.map { Value.Float(it) }
        )
    }
}

@Serializable
data class CineastRasterFeature(override val id: String, val hist: List<Float>, val raster: List<Float>) :
    CineastFeature {
    override fun toDescriptor(idmap: Map<String, String>): RasterDescriptor? {
        val id = this.id
        if (idmap[id] == null) {
            return null
        }
        return RasterDescriptor(
            id = DescriptorId.randomUUID(),
            retrievableId = RetrievableId.fromString(idmap[id]),
            hist = hist.map { Value.Float(it) },
            raster = raster.map { Value.Float(it) },
        )
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

@OptIn(InternalSerializationApi::class)
fun <T : Any> parseJsonList(filePath: String, clazz: KClass<T>): List<T> {
    val json = Json { ignoreUnknownKeys = true }
    return Files.newInputStream(Paths.get(filePath)).use { inputStream ->
        json.decodeFromStream(ListSerializer(clazz.serializer()), inputStream)
    }
}

@OptIn(InternalSerializationApi::class)
fun <T : Any> streamJsonItems(filePath: String, clazz: KClass<T>): Sequence<T> = sequence {
    val json = Json { ignoreUnknownKeys = true }
    Files.newInputStream(Paths.get(filePath)).use { inputStream ->
        val element = json.decodeFromStream<JsonElement>(inputStream)
        if (element is JsonArray) {
            element.forEach { jsonElement ->
                val item = Json.decodeFromJsonElement(clazz.serializer(), jsonElement)
                yield(item)
            }
        } else {
            throw IllegalArgumentException("Expected a JSON array at the root")
        }
    }
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

class CineastMigrationTool(val migrationconfigpath: String, val schemaconfigpath: String) {

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

        val objects: List<CineastMultimediaObject> =
            parseJsonList(migrationConfig.objectspath, CineastMultimediaObject::class)
        val retrievableWriter = schema.connection.getRetrievableWriter()
        val filemetadatawriter = schema.get("file")?.getWriter()
            ?: throw IllegalArgumentException("Could not find file metadata writer in schema ${schema.name}")

        //val retr_list = mutableListOf<Retrievable>()
        //val desc_list = mutableListOf<FileMetadataDescriptor>()

        //var i = 0
        for (mobject in objects) {
            val uuid = RetrievableId.randomUUID()
            idmap[mobject.objectid] = uuid.toString()
            val objectRetrievable = Ingested(
                uuid,
                "source",
                false
            )
            val size = 0L
            try {
                val size = Files.size(Paths.get(mobject.path))
            } catch (e: Exception) {
                println("Could not find file ${mobject.path}, setting size to 0")
            }
            val fileMetadataDescriptor = FileSourceMetadataDescriptor(
                id = DescriptorId.randomUUID(),
                retrievableId = objectRetrievable.id,
                path = Value.String(mobject.path),
                size = Value.Long(size),
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

        val objectmetadata: List<CineastObjectMetadata> =
            parseJsonList(migrationConfig.objectmetadatapath, CineastObjectMetadata::class)
        val mediadimensionswriter = schema.get("dimensions")?.getWriter()
            ?: throw IllegalArgumentException("Could not find media dimensions writer in schema ${schema.name}")
        val videofpswriter = schema.get("fps")?.getWriter()
            ?: throw IllegalArgumentException("Could not find video fps writer in schema ${schema.name}")
        val videodurationwriter = schema.get("duration")?.getWriter()
            ?: throw IllegalArgumentException("Could not find video duration writer in schema ${schema.name}")

        for (mobjectmetadata in objectmetadata) {
            val retrievableId = idmap[mobjectmetadata.objectid]
                ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}")

            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "width") {
                widths[retrievableId] = mobjectmetadata.value.toInt()
            }
            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "height") {
                val height = mobjectmetadata.value.toInt()
                heights[retrievableId] = height

                if (widths.containsKey(retrievableId)) {
                    val width = widths[retrievableId]
                        ?: throw IllegalArgumentException("Could not find width for object ${mobjectmetadata.objectid}")
                    val dimensionsDescriptor = MediaDimensionsDescriptor(
                        id = DescriptorId.randomUUID(),
                        retrievableId = RetrievableId.fromString(retrievableId),
                        width = Value.Int(width),
                        height = Value.Int(height)
                    )
                    mediadimensionswriter.add(dimensionsDescriptor)
                }
            }
            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "fps") {
                val fps = mobjectmetadata.value.toFloat()
                val retrievableId = idmap[mobjectmetadata.objectid]
                    ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}")
                val fpsDescriptor = FloatDescriptor(
                    id = DescriptorId.randomUUID(),
                    retrievableId = RetrievableId.fromString(retrievableId),
                    value = Value.Float(fps)
                )
                videofpswriter.add(fpsDescriptor)
            }

            if (mobjectmetadata.domain == "technical" && mobjectmetadata.key == "duration") {
                val retrievableId = idmap[mobjectmetadata.objectid]
                    ?: throw IllegalArgumentException("Could not find retrievable id for object ${mobjectmetadata.objectid}")
                val duration = mobjectmetadata.value.toFloat()
                val durationDescriptor = FloatDescriptor(
                    id = DescriptorId.randomUUID(),
                    retrievableId = RetrievableId.fromString(retrievableId),
                    value = Value.Float(duration)
                )
                videodurationwriter.add(durationDescriptor)
            }
        }
    }

    fun migrate_segments() {
        val segments: List<CineastSegment> = parseJsonList(migrationConfig.segmentspath, CineastSegment::class)
        val temporalmetadatawriter = schema.get("time")?.getWriter()
            ?: throw IllegalArgumentException("Could not find temporal metadata writer in schema ${schema.name}")
        val retrievableWriter = schema.connection.getRetrievableWriter()

        val batchSize = 5000
        val ingestedList = mutableListOf<Retrievable>()
        val temporalMetadataList = mutableListOf<TemporalMetadataDescriptor>()
        val parentIdList = mutableListOf<String>()

        for ((index, segment) in segments.withIndex()) {
            val uuid = RetrievableId.randomUUID()
            idmap[segment.segmentid] = uuid.toString()

            val ingested = Ingested(
                uuid,
                "segment",
                false
            )

            val temporalMetadataDescriptor = TemporalMetadataDescriptor(
                id = DescriptorId.randomUUID(),
                retrievableId = ingested.id,
                startNs = Value.Long(segment.segmentstartabs.toLong() * 1000 * 1000 * 1000),
                endNs = Value.Long(segment.segmentendabs.toLong() * 1000 * 1000 * 1000),
            )

            ingestedList.add(ingested)
            temporalMetadataList.add(temporalMetadataDescriptor)
            parentIdList.add(
                idmap[segment.objectid]
                    ?: throw IllegalArgumentException("Could not find retrievable id for segment ${segment.objectid}")
            )

            // Check if the batch size is reached
            if ((index + 1) % batchSize == 0 || index == segments.size - 1) {
                retrievableWriter.addAll(ingestedList)
                retrievableWriter.connectAll(
                    ingestedList.zip(parentIdList).map {
                        Relationship.ById(
                            it.first.id,
                            "isPartOf",
                            RetrievableId.fromString(it.second)
                                ?: throw IllegalArgumentException("Could not find retrievable id for segment $it"),
                            false
                        )
                    }
                )
                temporalmetadatawriter.addAll(temporalMetadataList)

                // Clear the lists for the next batch
                ingestedList.clear()
                temporalMetadataList.clear()
                parentIdList.clear()
            }
        }
    }

    fun migrate_segmentmetadata() {
        val segmentmetadata: List<CineastSegmentMetadata> =
            parseJsonList(migrationConfig.segmentmetadatapath, CineastSegmentMetadata::class)
        val dominantcolorwriter = schema.get("dominantcolor")?.getWriter()
            ?: throw IllegalArgumentException("Could not find dominant color writer in schema ${schema.name}")

        val dominantcolordescriptors = mutableListOf<StringDescriptor>()
        for (msegmentmetadata in segmentmetadata) {
            if (msegmentmetadata.domain == "dominantcolor" && msegmentmetadata.key == "color") {
                val color = msegmentmetadata.value
                try {
                    val retrievableId = RetrievableId.fromString(idmap[msegmentmetadata.segmentid])
                        ?: throw IllegalArgumentException("Could not find retrievable id for segment ${msegmentmetadata.segmentid}")
                    val dominantColorDescriptor = StringDescriptor(
                        id = DescriptorId.randomUUID(),
                        retrievableId = retrievableId,
                        value = Value.String(color)
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
        val featureConfig = migrationConfig.features.find { it.fieldname == fieldName }
            ?: throw IllegalArgumentException("Feature config for field $fieldName not found.")
        val path = featureConfig.path

        val field = schema.get(name = fieldName)
            ?: throw IllegalArgumentException("Field $fieldName does not exist in schema ${schema.name}.")

        val descriptorClass = field.analyser.descriptorClass

        val featureClass = getFeatureClassFromDescriptor(descriptorClass)
            ?: throw IllegalArgumentException("Feature class not found for descriptor class $descriptorClass")

        val featureStream = streamJsonItems(path, featureClass)

        val fieldwriter = field.getWriter()

        val fieldreader = field.getReader()

        if (fieldreader.count() > 0) {
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
                        writer.write(Json.encodeToString(feature.id))
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
    migrationTool.initialize()
    migrationTool.load_idmap()

    runBlocking {
        migrationTool.migrateFeature("skeletonpose")
    }

    println("Migration completed successfully.")
}
