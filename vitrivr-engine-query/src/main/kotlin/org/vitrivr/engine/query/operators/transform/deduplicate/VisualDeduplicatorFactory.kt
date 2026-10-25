package org.vitrivr.engine.query.operators.transform.deduplicate

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

/** Resolves database readers once; metadata is fetched in batches when the query executes. */
class VisualDeduplicatorFactory : OperatorFactory {
    override fun newOperator(
        name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context
    ): VisualDeduplicator {
        require(inputs.size == 1) { "VisualDeduplicator requires exactly one input." }
        val vectorFieldName = context[name, "field"] ?: "clip"
        val timeFieldName = context[name, "timeField"] ?: "time"
        val fileFieldName = context[name, "fileField"] ?: "file"
        val vectorField = context.schema[vectorFieldName]
            ?: throw IllegalArgumentException("Vector field '$vectorFieldName' not defined in schema.")
        val timeField = context.schema[timeFieldName]
            ?: throw IllegalArgumentException("Time field '$timeFieldName' not defined in schema.")
        val fileField = context.schema[fileFieldName]
            ?: throw IllegalArgumentException("File field '$fileFieldName' not defined in schema.")
        require(vectorField.analyser.descriptorClass == FloatVectorDescriptor::class) { "field must store float vectors." }
        require(timeField.analyser.descriptorClass == TemporalMetadataDescriptor::class) { "timeField must store temporal metadata." }
        require(fileField.analyser.descriptorClass == FileSourceMetadataDescriptor::class) { "fileField must store file metadata." }

        fun doubleParameter(key: String, default: Double): Double = context[name, key]?.let {
            it.toDoubleOrNull() ?: throw IllegalArgumentException("$key must be a number.")
        } ?: default
        val limit = context[name, "limit"]?.let {
            it.toIntOrNull() ?: throw IllegalArgumentException("limit must be an integer.")
        } ?: 20
        val metadata = VisualDeduplicationMetadata(vectorField.getReader(), timeField.getReader(),
            fileField.getReader(), context.schema.connection.getRetrievableReader())
        return VisualDeduplicator(inputs.values.single(), metadata::load,
            doubleParameter("similarityThreshold", 0.98), doubleParameter("timeWindowSeconds", 60.0), limit, name)
    }
}
