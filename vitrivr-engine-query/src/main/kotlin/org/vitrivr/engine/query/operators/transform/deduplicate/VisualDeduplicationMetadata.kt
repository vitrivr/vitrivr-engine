package org.vitrivr.engine.query.operators.transform.deduplicate

import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/** Reads stored vectors internally, without adding large embedding arrays to API results. */
internal class VisualDeduplicationMetadata(
    private val vectors: DescriptorReader<*>,
    private val times: DescriptorReader<*>,
    private val files: DescriptorReader<*>,
    private val retrievables: RetrievableReader
) {
    fun load(ids: Set<RetrievableId>): Map<RetrievableId, VisualDeduplicator.Signature> {
        // Empty getConnections arguments mean "all", so never query it for an empty batch.
        if (ids.isEmpty()) return emptyMap()
        val embeddings = vectors.getAllForRetrievable(ids).filterIsInstance<FloatVectorDescriptor>()
            .groupBy { it.retrievableId }
        val timestamps = times.getAllForRetrievable(ids).filterIsInstance<TemporalMetadataDescriptor>()
            .groupBy { it.retrievableId }
        val paths = files.getAllForRetrievable(ids).filterIsInstance<FileSourceMetadataDescriptor>()
            .groupBy { it.retrievableId }.mapValues { (_, descriptors) -> descriptors.map { it.path.value }.distinct() }
        val missingPaths = ids.filter { it !in paths }
        val parents = if (missingPaths.isEmpty()) emptyMap() else
            retrievables.getConnections(missingPaths, listOf("partOf"), emptyList()).groupBy { it.subjectId }
        val parentIds = parents.values.flatten().map { it.objectId }.toSet()
        val parentPaths = if (parentIds.isEmpty()) emptyMap() else
            files.getAllForRetrievable(parentIds).filterIsInstance<FileSourceMetadataDescriptor>()
                .groupBy { it.retrievableId }.mapValues { (_, descriptors) -> descriptors.map { it.path.value }.distinct() }
        return ids.mapNotNull { id ->
            val vector = embeddings[id]?.singleOrNull()?.vector?.value ?: return@mapNotNull null
            val start = timestamps[id]?.map { it.start.value }?.distinct()?.singleOrNull() ?: return@mapNotNull null
            val sourcePaths = paths[id] ?: parents[id].orEmpty().flatMap { parentPaths[it.objectId].orEmpty() }.distinct()
            val path = sourcePaths.singleOrNull() ?: return@mapNotNull null
            id to VisualDeduplicator.Signature(path, start, vector)
        }.toMap()
    }
}
