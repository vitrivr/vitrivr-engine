package org.vitrivr.engine.core.features.metadata.file

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.FileMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource
import java.nio.file.Files
import java.util.*
import kotlin.io.path.absolutePathString

/**
 * An [Extractor] that extracts [FileMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileMetadataExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, FileMetadataDescriptor>, persisting: Boolean = true) : AbstractExtractor<ContentElement<*>, FileMetadataDescriptor>(input, field, persisting) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileMetadataExtractor] implementation only works with [RetrievableWithSource] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable is RetrievableWithSource && retrievable.source is FileSource

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FileMetadataDescriptor> {
        check(retrievable is RetrievableWithSource) { "Incoming retrievable is not a retrievable with source. This is a programmer's error!" }
        check(retrievable.source is FileSource) { "Incoming retrievable is not a retrievable with file source. This is a programmer's error!" }
        return listOf(
            FileMetadataDescriptor(
                id = UUID.randomUUID(),
                retrievableId = retrievable.id,
                path = (retrievable.source as FileSource).path.absolutePathString(),
                size = Files.size((retrievable.source as FileSource).path),
                transient = !persisting
            )
        )
    }
}