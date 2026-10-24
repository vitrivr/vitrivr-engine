package org.vitrivr.engine.core.features.metadata.recording

/** Shared rules with scripts/backfill_recording_metadata.py: /.../dayN/source/.../file. */
data class RecordingPath(val day: String, val source: String, val sourceType: String) {
    companion object {
        fun parse(path: String, persons: Set<String> = emptySet(), locations: Set<String> = emptySet()): RecordingPath? {
            require(persons.intersect(locations).isEmpty()) { "A recording source cannot be both person and room." }
            val parts = path.split('/').filter { it.isNotEmpty() }
            val indices = parts.indices.filter { parts[it].matches(Regex("day[0-9]+")) && it + 2 < parts.size }
            if (indices.size != 1) return null
            val index = indices.single()
            val source = parts[index + 1]
            if (source.length > 255 || parts[index].length > 255) return null
            return RecordingPath(parts[index], source, when (source) {
                in persons -> "person"
                in locations -> "room"
                else -> "unknown"
            })
        }
    }
}
