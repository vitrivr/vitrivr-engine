package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [StructDescriptor] used to store metadata about a file.
 *
 * @author Ralph Gasser
 * @version 2.1.0
 */
class FileSourceMetadataDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, FileSourceMetadataDescriptor>? = null
) : StructDescriptor<FileSourceMetadataDescriptor>(id, retrievableId, SCHEMA, values, field) {

    constructor(id: DescriptorId, retrievableId: RetrievableId?, path: Value.String, size: Value.Long, field: Schema.Field<*, FileSourceMetadataDescriptor>) :
            this(id, retrievableId, mapOf("path" to path, "size" to size), field)

    /** The path to the file. */
    val path: Value.String by this.values

    /** The size of the file in bytes. */
    val size: Value.Long by this.values

    companion object {
        /** The field schema associated with a [FileSourceMetadataDescriptor]. */
        private val SCHEMA = listOf(
            Attribute("path", Type.String),
            Attribute("size", Type.Long),
        )

        /** The prototype [FileSourceMetadataDescriptor]. */
        val PROTOTYPE = FileSourceMetadataDescriptor(UUID.randomUUID(), UUID.randomUUID(), mapOf("path" to Value.String(""), "size" to Value.Long(0L)))
    }

    /**
     * Returns a copy of this [FileSourceMetadataDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [FileSourceMetadataDescriptor].
     * @param retrievableId [RetrievableId] of the new [FileSourceMetadataDescriptor].
     * @param field [Schema.Field] the new [FileSourceMetadataDescriptor] belongs to.
     * @return Copy of this [FileSourceMetadataDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, FileSourceMetadataDescriptor>?) = FileSourceMetadataDescriptor(id, retrievableId, HashMap(this.values), field)
}