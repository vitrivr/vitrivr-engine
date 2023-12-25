package org.vitrivr.engine.core.features.metadata.source.file

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [FileSourceMetadataDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileSourceMetadataRetriever(override val field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, private val context: QueryContext) : Retriever<ContentElement<*>, FileSourceMetadataDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        TODO("Not yet implemented")
    }
}