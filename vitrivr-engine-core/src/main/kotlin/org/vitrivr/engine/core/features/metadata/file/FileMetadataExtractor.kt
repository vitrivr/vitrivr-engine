package org.vitrivr.engine.core.features.metadata.file

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.FileMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
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
class FileMetadataExtractor(override val field: Schema.Field<ContentElement<*>, FileMetadataDescriptor>, override val input: Operator<Retrievable>, override val persisting: Boolean = true) : Extractor<ContentElement<*>, FileMetadataDescriptor> {

    /** */
    private val writer by lazy { this.field.getWriter() }

    /**
     *
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { retrievable ->
        if (retrievable is RetrievableWithSource) {
            val source = retrievable.source
            if (source is FileSource) {
                val descriptor = FileMetadataDescriptor(
                    id = UUID.randomUUID(),
                    retrievableId = retrievable.id,
                    path = source.path.absolutePathString(),
                    size = Files.size(source.path),
                    transient = !persisting
                )

                /* Append descriptor. */
                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    retrievable.addDescriptor(descriptor)
                }

                /* Persist descriptor. */
                if (this.persisting) {
                    this.writer.add(descriptor)
                }
            }
        }
        retrievable
    }
}