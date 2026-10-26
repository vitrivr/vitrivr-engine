package org.vitrivr.engine.database.pgvector.descriptor

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.FaceIdentityDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.filter.EarlyFilter
import org.vitrivr.engine.core.model.query.filter.PersonFilter
import org.vitrivr.engine.database.pgvector.exposed.functions.plainToTsQuery
import org.vitrivr.engine.database.pgvector.exposed.ops.tsMatches
import org.vitrivr.engine.database.pgvector.toTable
import java.util.UUID

/** Compiles every leaf to an unlimited ID subquery, then combines membership with SQL Boolean logic. */
internal class EarlyFilterSql(private val schema: Schema, private val id: Column<EntityID<UUID>>) {
    fun compile(filter: EarlyFilter): Op<Boolean> = when (filter) {
        is EarlyFilter.All -> filter.conditions.map { compile(it) }.compoundAnd()
        is EarlyFilter.Any -> filter.conditions.map { compile(it) }.compoundOr()
        is EarlyFilter.Not -> NotOp(compile(filter.condition))
        is EarlyFilter.Recording -> recording(filter)
        is EarlyFilter.Persons -> persons(filter)
        is EarlyFilter.Text -> text(filter)
        is EarlyFilter.Exists -> {
            val table = field(filter.field).toTable()
            SqlExpressionBuilder.run { id inSubQuery table.select(table.retrievableId) }
        }
    }

    private fun field(name: String) = schema[name] ?: throw IllegalArgumentException("Filter field '$name' not defined in schema.")

    @Suppress("UNCHECKED_CAST")
    private fun recording(filter: EarlyFilter.Recording): Op<Boolean> {
        val field = field(filter.field)
        require(field.analyser.descriptorClass == RecordingMetadataDescriptor::class) { "Recording filters require a RecordingMetadata field." }
        val table = field.toTable()
        val constraints = filter.values.map { (name, values) ->
            val column = table.columns.single { it.name == name } as Column<String>
            SqlExpressionBuilder.run { column inList values }
        }
        val eligible = table.select(table.retrievableId).where { constraints.compoundAnd() }
        return SqlExpressionBuilder.run { id inSubQuery eligible }
    }

    @Suppress("UNCHECKED_CAST")
    private fun persons(filter: EarlyFilter.Persons): Op<Boolean> {
        val persons = PersonFilter(filter.names, filter.field, filter.mode, filter.minConfidence)
        val field = field(persons.field)
        require(field.analyser.descriptorClass == FaceIdentityDescriptor::class) { "Person filters require a FaceIdentityDescriptor field." }
        val table = field.toTable()
        val label = table.columns.single { it.name == FaceIdentityDescriptor.LABEL_FIELD_NAME } as Column<String>
        val eligible = table.select(table.retrievableId).where { label inList persons.normalizedNames }
        persons.minConfidence?.let { minimum ->
            val confidence = table.columns.single { it.name == FaceIdentityDescriptor.CONFIDENCE_FIELD_NAME } as Column<Float>
            eligible.andWhere { confidence greaterEq minimum.toFloat() }
        }
        if (persons.mode == PersonFilter.Mode.ALL) {
            eligible.groupBy(table.retrievableId).having { label.countDistinct() eq persons.normalizedNames.size.toLong() }
        }
        return SqlExpressionBuilder.run { id inSubQuery eligible }
    }

    @Suppress("UNCHECKED_CAST")
    private fun text(filter: EarlyFilter.Text): Op<Boolean> {
        val field = field(filter.field)
        require(field.analyser.descriptorClass == TextDescriptor::class) { "Text filters require a TextDescriptor field (e.g. OCR or ASR)." }
        val table = field.toTable()
        val column = table.columns.single { it.name == "descriptor" } as Column<String>
        val eligible = table.select(table.retrievableId).where {
            when (filter.match) {
                EarlyFilter.TextMatch.FULLTEXT -> column tsMatches plainToTsQuery(stringParam(filter.text))
                EarlyFilter.TextMatch.EXACT -> column eq filter.text
                EarlyFilter.TextMatch.CONTAINS -> {
                    val literal = filter.text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                    LikeEscapeOp(column, stringParam("%$literal%"), true, '\\')
                }
            }
        }
        return SqlExpressionBuilder.run { id inSubQuery eligible }
    }
}
