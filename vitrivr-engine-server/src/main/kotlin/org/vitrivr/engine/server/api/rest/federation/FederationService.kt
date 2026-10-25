package org.vitrivr.engine.server.api.rest.federation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.types.ValueContentOnlySerializer
import org.vitrivr.engine.core.model.descriptor.struct.FaceIdentityDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.filter.MetadataFilter
import org.vitrivr.engine.core.model.query.filter.MetadataFilteredQuery
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID

/** Read-only adapter for systems using different segment boundaries. */
class FederationService(private val schema: Schema) {
    fun lookup(request: FederationLookupRequest): FederationLookupResponse {
        request.validate()
        val recording = field(request.recordingField)
        val files = field(request.fileField)
        val times = field(request.timeField)
        require(recording.analyser.descriptorClass == RecordingMetadataDescriptor::class) { "recordingField must be RecordingMetadata." }
        require(files.analyser.descriptorClass == FileSourceMetadataDescriptor::class) { "fileField must be FileSourceMetadata." }
        require(times.analyser.descriptorClass == TemporalMetadataDescriptor::class) { "timeField must be TemporalMetadata." }
        val filters = linkedMapOf("source" to listOf(request.source), "day" to listOf(request.day))
        request.sourceType?.let { filters["source_type"] = listOf(it) }
        // A full path is exact. A basename is matched as a literal last path component.
        val fullPath = '/' in request.video
        val video = if (fullPath) request.video else "%/" + request.video
            .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        val query = MetadataFilteredQuery(SimpleBooleanQuery(Value.String(video),
            comparison = if (fullPath) ComparisonOperator.EQ else ComparisonOperator.LIKE, attributeName = "path"),
            MetadataFilter(request.recordingField, filters))
        val fileDescriptors = files.getReader().query(query).filterIsInstance<FileSourceMetadataDescriptor>().toList()
        val paths = fileDescriptors.groupBy { it.retrievableId!! }.mapValues { (_, values) -> values.map { it.path.value }.toSet() }
        if (paths.isEmpty()) return FederationLookupResponse(emptyList(), false)

        // File metadata may live on a source video; decoded segments point to it via partOf.
        // Restrict the relation lookup to those video/result IDs (empty means unbounded).
        val descendants = schema.connection.getRetrievableReader()
            .getConnections(emptyList(), listOf("partOf"), paths.keys).toList()
        val candidatePaths = paths.mapValues { it.value.toMutableSet() }.toMutableMap()
        for (relation in descendants) {
            candidatePaths.getOrPut(relation.subjectId) { mutableSetOf() }.addAll(paths[relation.objectId].orEmpty())
        }
        val startNs = (request.timeMs ?: request.startMs!!) * 1_000_000L
        val endNs = request.endMs?.times(1_000_000L)
        val matches = times.getReader().getAllForRetrievable(candidatePaths.keys)
            .filterIsInstance<TemporalMetadataDescriptor>().flatMap { temporal ->
                val start = temporal.start.value
                val end = temporal.end.value
                if (!overlaps(start, end, startNs, endNs)) return@flatMap emptySequence()
                val overlap = if (endNs == null) 0.0 else
                    (minOf(end, endNs) - maxOf(start, startNs)).coerceAtLeast(0L).toDouble() / 1_000_000.0
                candidatePaths[temporal.retrievableId].orEmpty().asSequence().map { path ->
                    FederationSegmentMatch(temporal.retrievableId.toString(), path,
                        start.toDouble() / 1_000_000.0, end.toDouble() / 1_000_000.0, overlap)
                }
            }.distinct().sortedWith(compareBy({ it.startMs }, { it.videoPath }, { it.retrievableId }, { it.endMs })).toList()
        return FederationLookupResponse(matches.take(request.limit), matches.size > request.limit)
    }

    fun features(request: FederationFeaturesRequest): FederationFeaturesResponse {
        request.validate()
        val ids = parseIds(request.retrievableIds)
        // Resolve every field before reading data, including when no requested IDs exist.
        val requestedFields = request.fields.distinct().associateWith { field(it) }
        val timeField = request.timeField?.let { name -> field(name).also {
            require(it.analyser.descriptorClass == TemporalMetadataDescriptor::class) { "timeField must be TemporalMetadata." }
        } }
        val existing = schema.connection.getRetrievableReader().getAll(ids).map { it.id }.toSet()
        val stored = requestedFields.mapValues { (_, field) ->
            if (existing.isEmpty()) emptyMap() else
                field.getReader().getAllForRetrievable(existing).groupBy { it.retrievableId }
        }
        val times = if (timeField == null || existing.isEmpty()) emptyMap() else {
            val loaded = stored[timeField.fieldName]?.values?.flatten()?.asSequence()
                ?: timeField.getReader().getAllForRetrievable(existing)
            loaded.filterIsInstance<TemporalMetadataDescriptor>().groupBy { it.retrievableId }
        }
        val person = request.person?.let { FaceIdentityDescriptor.normalizeLabel(it) }
        return FederationFeaturesResponse(ids.map { id ->
            FederationFeatureResult(id.toString(), if (id in existing) "available" else "not_found",
                times[id].orEmpty().map { FederationTimeRange(it.start.value / 1_000_000.0, it.end.value / 1_000_000.0) }
                    .distinct().sortedWith(compareBy({ it.startMs }, { it.endMs })),
                requestedFields.mapValues { (name, field) ->
                    val descriptors = stored[name]?.get(id).orEmpty().sortedBy { it.id.toString() }
                    FederationFeatureField(when {
                        id !in existing -> "not_found"
                        descriptors.isEmpty() -> "no_descriptors"
                        else -> "available"
                    }, descriptors.map { featureDescriptor(it, request.includeVectors) },
                        if (field.analyser.descriptorClass == FaceIdentityDescriptor::class)
                            summarizeIdentity(id, id in existing, descriptors.filterIsInstance<FaceIdentityDescriptor>(), person, request.minConfidence)
                        else null)
                })
        })
    }

    private fun featureDescriptor(descriptor: Descriptor<*>, includeVectors: Boolean): FederationFeatureDescriptor {
        val omitted = mutableListOf<String>()
        val values = descriptor.values().mapNotNull { (name, value) ->
            if (!includeVectors && value is Value.Vector<*>) {
                omitted.add(name)
                null
            } else name to (value?.let { Json.encodeToJsonElement(ValueContentOnlySerializer, it) } ?: JsonNull)
        }.toMap()
        return FederationFeatureDescriptor(descriptor.id.toString(), JsonObject(values), omitted)
    }

    /** Builds the optional face identity summary for a requested feature field. */
    private fun summarizeIdentity(id: UUID, exists: Boolean, stored: List<FaceIdentityDescriptor>,
                                  person: String?, minConfidence: Float): FederationIdentityResult {
        val evidence = stored.filter { it.confidence.value.isFinite() && it.confidence.value >= minConfidence }
            .map { descriptor ->
                val label = FaceIdentityDescriptor.normalizeLabel(descriptor.label.value)
                FederationIdentity(label, descriptor.confidence.value,
                    label.isNotBlank() && !label.equals("unknown", ignoreCase = true))
            }.distinct().sortedWith(compareByDescending<FederationIdentity> { it.confidence }.thenBy { it.label })
        return FederationIdentityResult(id.toString(), when {
            !exists -> "not_found"
            stored.isEmpty() -> "no_descriptors"
            else -> "available"
        }, stored.size, evidence.any { it.recognized },
            if (person == null || !exists) null else evidence.any { it.recognized && it.label == person }, evidence)
    }

    private fun parseIds(values: List<String>): List<UUID> = values.map { id ->
        require(id.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))) {
            "Invalid retrievable UUID: $id"
        }
        UUID.fromString(id)
    }.distinct()

    private fun field(name: String) = schema[name] ?: throw IllegalArgumentException("Field '$name' not defined in schema.")

    companion object {
        /** Half-open intervals avoid attributing a boundary time to the preceding segment. */
        internal fun overlaps(start: Long, end: Long, queryStart: Long, queryEnd: Long?): Boolean {
            if (end < start) return false
            return if (queryEnd == null) {
                if (start == end) start == queryStart else start <= queryStart && queryStart < end
            } else {
                if (start == end) queryStart <= start && start < queryEnd else start < queryEnd && end > queryStart
            }
        }
    }
}
