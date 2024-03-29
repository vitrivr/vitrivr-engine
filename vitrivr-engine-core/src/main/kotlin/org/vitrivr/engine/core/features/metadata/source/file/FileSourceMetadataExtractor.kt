package org.vitrivr.engine.core.features.metadata.source.file

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource
import java.nio.file.Files
import java.util.*
import kotlin.io.path.absolutePathString

/**
 * An [Extractor] that extracts [FileSourceMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileSourceMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, FileSourceMetadataDescriptor>(input, field, persisting, bufferSize = 1) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [Retrievable] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FileSourceMetadataDescriptor> {
//        check(retrievable is RetrievableWithSource) { "Incoming retrievable is not a retrievable with source. This is a programmer's error!" }
//        check(retrievable.source is FileSource) { "Incoming retrievable is not a retrievable with file source. This is a programmer's error!" }
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source as? FileSource
            ?: throw IllegalArgumentException("Incoming retrievable is not a retrievable with file source. This is a programmer's error!")
        return listOf(
            FileSourceMetadataDescriptor(
                id = UUID.randomUUID(),
                retrievableId = retrievable.id,
                path = source.path.absolutePathString(),
                size = Files.size(source.path),
                transient = !persisting
            )
        )
    }
}