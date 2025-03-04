package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import java.util.stream.Stream

interface ShotBoundaryProvider {
    fun decode(boundaryId: String): List<MediaSegmentDescriptor>
}

/**
 * A factory object for a specific [Enumerator] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface ShotBoundaryProviderFactory {
    fun newShotBoundaryProvider(uri: String, parmeters: Map<String,String> = mapOf()): ShotBoundaryProvider
}
