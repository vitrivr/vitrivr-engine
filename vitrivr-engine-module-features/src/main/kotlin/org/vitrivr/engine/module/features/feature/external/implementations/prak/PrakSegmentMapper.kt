package org.vitrivr.engine.module.features.feature.external.implementations.prak

import org.vitrivr.engine.core.database.descriptor.TemporalLookupPoint
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.filter.MetadataFilter
import org.vitrivr.engine.core.model.query.filter.MetadataFilteredQuery
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID

/** Request-scoped cache; metadata is loaded in bounded batches of videos, never for the entire database. */
internal class PrakSegmentMapper(private val store: PrakMappingStore, private val batchSize: Int = 128) {
    constructor(schema: Schema, fileField: String, timeField: String, recordingField: String = "recording") :
        this(SchemaPrakMappingStore(schema, fileField, timeField, recordingField))

    private val pointCache = mutableMapOf<Pair<String, Double>, List<UUID>>()
    private var usePointLookup = true
    private val cache = mutableMapOf<String, List<TemporalMetadataDescriptor>>()

    init { require(batchSize > 0) }

    fun mapResults(results: List<PrakApiResult>): List<Pair<PrakApiResult, List<UUID>>> {
        if (usePointLookup) {
            val points = results.map { result ->
                val video = result.id.first()
                val recording = recording(video)
                TemporalLookupPoint(video, result.time[1].toDouble(), recording?.get(0), recording?.get(1), recording?.get(2)?.let { "$it.mp4" })
            }
            val missing = points.distinctBy { it.video to it.timeMs }.filterNot { (it.video to it.timeMs) in pointCache }
            val mapped = if (missing.isEmpty()) emptyList() else store.lookupPoints(missing)
            if (mapped != null) {
                require(mapped.size == missing.size) { "Temporal lookup returned an incomplete point mapping." }
                missing.zip(mapped).forEach { (point, ids) -> pointCache[point.video to point.timeMs] = ids }
                return results.zip(points).map { (result, point) -> result to pointCache.getValue(point.video to point.timeMs) }
            }
            usePointLookup = false
        }
        prepare(results)
        return results.map { it to match(it) }
    }

    private fun prepare(results: List<PrakApiResult>) {
        results.mapNotNull { it.id.firstOrNull() }.distinct().filterNot { it in cache }
            .chunked(batchSize).forEach { load(it) }
    }

    fun match(result: PrakApiResult): List<UUID> {
        val video = result.id.firstOrNull() ?: return emptyList()
        val timeMs = result.time.getOrNull(1)?.toDoubleOrNull() ?: return emptyList()
        if (!timeMs.isFinite() || timeMs < 0) return emptyList()
        if (video !in cache) load(listOf(video))
        return cache.getValue(video).filter { contains(it.start.value, it.end.value, timeMs) }
            .mapNotNull { it.retrievableId }.distinct().sortedBy { it.toString() }
    }

    private fun load(videos: List<String>) {
        val queries = videos.flatMap { video ->
            require(video.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid PraK video ID '$video'." }
            val basename = video.replace("_", "\\_") + ".mp4"
            val flat = listOf<Query>(
                SimpleBooleanQuery(Value.String("%/$basename"), comparison = ComparisonOperator.LIKE, attributeName = "path"),
                SimpleBooleanQuery(Value.String("$video.mp4"), attributeName = "path"))
            val recording = recording(video)
            if (recording == null) flat else {
                val (day, source, hour) = recording
                val nested = SimpleBooleanQuery(Value.String("%/$hour.mp4"), comparison = ComparisonOperator.LIKE, attributeName = "path")
                flat + if (store.recordingField == null) nested else MetadataFilteredQuery(nested,
                    MetadataFilter(store.recordingField!!, mapOf("day" to listOf(day), "source" to listOf(source))))
            }
        }
        val files = store.files(queries.distinct())
        val fileIds = files.mapNotNull { it.retrievableId }.toSet()
        // Empty relation inputs mean an unbounded scan in the reader API; never pass an empty set.
        if (fileIds.isEmpty()) {
            videos.forEach { cache[it] = emptyList() }
            return
        }
        val recordings = if (store.recordingField == null) emptyMap() else readPrakIdBatches(fileIds, store::recordings).groupBy { it.retrievableId }
        val requested = videos.toSet()
        val sourceIndex = mutableMapOf<String, MutableSet<UUID>>()
        fun index(video: String, id: UUID) {
            if (video in requested) sourceIndex.getOrPut(video) { mutableSetOf() }.add(id)
        }
        // Index every file once rather than rescanning all metadata for each video.
        files.forEach { file ->
            val id = file.retrievableId ?: return@forEach
            val basename = file.path.value.substringAfterLast('/')
            if (!basename.endsWith(".mp4")) return@forEach
            val stem = basename.removeSuffix(".mp4")
            index(stem, id)
            if (store.recordingField != null) {
                recordings[id].orEmpty().forEach { index("${it.day.value}_${it.source.value}_$stem", id) }
            } else {
                val parts = file.path.value.split('/')
                parts.indices.filter { i -> i + 2 < parts.size && parts[i].matches(Regex("day[1-4]")) }
                    .forEach { i -> index("${parts[i]}_${parts[i + 1]}_$stem", id) }
            }
        }
        val sourcesByVideo = videos.associateWith { sourceIndex[it].orEmpty() }
        val sources = sourcesByVideo.values.flatten().toSet()
        if (sources.isEmpty()) {
            videos.forEach { cache[it] = emptyList() }
            return
        }
        val relations = readPrakIdBatches(sources, store::children)
        val childrenBySource = relations.groupBy({ it.objectId }, { it.subjectId })
        val temporalById = readPrakIdBatches(sources + relations.map { it.subjectId }, store::times).groupBy { it.retrievableId }
        sourcesByVideo.forEach { (video, ids) ->
            val segments = ids + ids.flatMap { childrenBySource[it].orEmpty() }
            cache[video] = segments.flatMap { temporalById[it].orEmpty() }
        }
    }

    companion object {
        internal fun recording(video: String): List<String>? = Regex("(day[1-4])_(.+)_([0-9]{1,2})")
            .matchEntire(video)?.groupValues?.drop(1)

        internal fun contains(startNs: Long, endNs: Long, timeMs: Double): Boolean {
            val ns = timeMs * 1_000_000.0
            return if (startNs == endNs) ns == startNs.toDouble() else startNs <= ns && ns < endNs
        }
    }
}

/** Narrow persistence adapter also permits mapping tests without a running database. */
internal interface PrakMappingStore {
    val recordingField: String?
    fun lookupPoints(points: List<TemporalLookupPoint>): List<List<UUID>>? = null
    fun files(queries: List<Query>): List<FileSourceMetadataDescriptor>
    fun recordings(ids: Set<UUID>): List<RecordingMetadataDescriptor>
    fun children(ids: Set<UUID>): List<Relationship.ById>
    fun times(ids: Set<UUID>): List<TemporalMetadataDescriptor>
}

private class SchemaPrakMappingStore(private val schema: Schema, fileField: String, timeField: String, recordingField: String) : PrakMappingStore {
    private val files = requireNotNull(schema[fileField]) { "Missing PraK file field '$fileField'." }
    private val times = requireNotNull(schema[timeField]) { "Missing PraK time field '$timeField'." }
    private val recording = schema[recordingField]
    override val recordingField = recording?.fieldName

    init {
        require(files.analyser.descriptorClass == FileSourceMetadataDescriptor::class)
        require(times.analyser.descriptorClass == TemporalMetadataDescriptor::class)
        require(recording == null || recording.analyser.descriptorClass == RecordingMetadataDescriptor::class)
    }

    override fun lookupPoints(points: List<TemporalLookupPoint>) = times.getReader().lookupTemporalPoints(points, files.fieldName, recordingField)
    override fun files(queries: List<Query>) = files.getReader().queryAny(queries).filterIsInstance<FileSourceMetadataDescriptor>().toList()
    override fun recordings(ids: Set<UUID>) = recording!!.getReader().getAllForRetrievable(ids).filterIsInstance<RecordingMetadataDescriptor>().toList()
    override fun children(ids: Set<UUID>) = schema.connection.getRetrievableReader().getConnections(emptyList(), listOf("partOf"), ids).toList()
    override fun times(ids: Set<UUID>) = times.getReader().getAllForRetrievable(ids).filterIsInstance<TemporalMetadataDescriptor>().toList()
}

/** Video batches can fan out to many more segment IDs. Bound every resulting SQL IN list as well. */
internal fun <T> readPrakIdBatches(ids: Collection<UUID>, read: (Set<UUID>) -> List<T>): List<T> =
    ids.chunked(10_000).flatMap { read(it.toSet()) }
