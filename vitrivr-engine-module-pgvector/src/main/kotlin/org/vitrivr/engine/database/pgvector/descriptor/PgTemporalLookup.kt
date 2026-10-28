package org.vitrivr.engine.database.pgvector.descriptor

import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.descriptor.TemporalLookupPoint
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.toTable
import java.util.UUID

/** Join requested timestamps to stored metadata in PostgreSQL; return only matching IDs. */
internal class PgTemporalLookup(private val temporal: Schema.Field<*, *>, private val connection: PgVectorConnection) {
    fun lookup(points: List<TemporalLookupPoint>, fileField: String, recordingField: String?): List<List<UUID>> {
        require(temporal.analyser.descriptorClass == TemporalMetadataDescriptor::class)
        val files = requireNotNull(temporal.schema[fileField]) { "Missing file field '$fileField'." }
        require(files.analyser.descriptorClass == FileSourceMetadataDescriptor::class)
        val recording = recordingField?.let { requireNotNull(temporal.schema[it]) }
        require(recording == null || recording.analyser.descriptorClass == RecordingMetadataDescriptor::class)
        if (points.isEmpty()) return emptyList()
        // Each point/video contributes at most eight bind parameters; no ID fan-out is sent back into SQL.
        return points.chunked(1000).flatMap { chunk ->
            transaction(connection.database) {
                val videos = chunk.distinctBy { it.video }
                require(chunk.groupBy { it.video }.values.all { entries ->
                    entries.map { Triple(it.day, it.source, it.filename) }.distinct().size == 1
                }) { "Conflicting recording identities for a video." }
                val args = mutableListOf<Pair<IColumnType<*>, Any?>>()
                videos.forEach { point ->
                    listOf(point.video, "${point.video}.mp4", point.day, point.source, point.filename).forEach {
                        args.add(TextColumnType() to it)
                    }
                }
                chunk.forEachIndexed { index, point ->
                    args.add(IntegerColumnType() to index)
                    args.add(TextColumnType() to point.video)
                    args.add(DoubleColumnType() to point.timeMs * 1_000_000.0)
                }
                // Names come from resolved schema fields and are quoted separately from bound input values.
                fun name(raw: String) = "\"${raw.replace("\"", "\"\"")}\""
                fun table(field: Schema.Field<*, *>) = name(temporal.schema.name.lowercase()) + "." + name(field.toTable().tableName)
                val fileTable = table(files)
                val timeTable = table(temporal)
                val relationTable = name(temporal.schema.name.lowercase()) + ".\"relationships\""
                val nestedJoin = if (recording != null) """
                    JOIN videos v ON reverse(split_part(reverse(f.path), '/', 1)) = v.filename
                      AND f.day = v.day AND f.source = v.source
                """ else """
                    JOIN videos v ON reverse(split_part(reverse(f.path), '/', 1)) = v.filename
                      AND position('/' || v.day || '/' || v.source || '/' in '/' || f.path) > 0
                """
                // Resolve recording identity before joining repeated hour filenames. OFFSET 0 keeps
                // the planner from probing recording metadata once per (file, candidate video) pair.
                val nestedFiles = if (recording != null) """
                    (SELECT f.retrievableid, f.path, m.day, m.source FROM $fileTable f
                     JOIN ${table(recording)} m ON m.retrievableid = f.retrievableid OFFSET 0) f
                """ else "$fileTable f"
                val sql = """
                    WITH videos(video, flat, day, source, filename) AS (VALUES ${videos.joinToString { "(?::text,?::text,?::text,?::text,?::text)" }}),
                    points(idx, video, ns) AS (VALUES ${chunk.joinToString { "(?::int,?::text,?::double precision)" }}),
                    files AS MATERIALIZED (
                        SELECT v.video, f.retrievableid FROM $fileTable f
                        JOIN videos v ON reverse(split_part(reverse(f.path), '/', 1)) = v.flat
                        UNION
                        SELECT v.video, f.retrievableid FROM $nestedFiles $nestedJoin
                    ), segments AS (
                        SELECT video, retrievableid FROM files
                        UNION
                        SELECT f.video, r."subjectId" FROM files f JOIN $relationTable r
                          ON r."objectId" = f.retrievableid AND r.predicate = 'partOf'
                    )
                    SELECT DISTINCT p.idx, t.retrievableid FROM segments s
                    JOIN $timeTable t ON t.retrievableid = s.retrievableid
                    JOIN points p ON p.video = s.video AND
                      ((t."start" <= p.ns AND p.ns < t."end") OR (t."start" = t."end" AND t."start" = p.ns))
                """.trimIndent()
                val matched = List(chunk.size) { mutableSetOf<UUID>() }
                exec(sql, args, explicitStatementType = StatementType.SELECT) { rows ->
                    while (rows.next()) matched[rows.getInt(1)].add(rows.getObject(2, UUID::class.java))
                }
                matched.map { it.sortedBy(UUID::toString) }
            }
        }
    }
}
