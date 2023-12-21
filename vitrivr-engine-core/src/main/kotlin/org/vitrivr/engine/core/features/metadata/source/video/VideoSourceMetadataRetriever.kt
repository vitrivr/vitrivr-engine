package org.vitrivr.engine.core.features.metadata.source.video

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [VideoSourceMetadataDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoSourceMetadataRetriever(override val field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, private val context: QueryContext) : Retriever<ContentElement<*>, VideoSourceMetadataDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        TODO("Not yet implemented")
    }
}