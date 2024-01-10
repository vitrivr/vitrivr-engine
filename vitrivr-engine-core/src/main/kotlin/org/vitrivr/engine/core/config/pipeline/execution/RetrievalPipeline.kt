package org.vitrivr.engine.core.config.pipeline.execution

import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

/**
 * A pipeline for retrieval. It wraps a (query) [Operator] that returns [Retrieved] objects.
 *
 * TODO: Builder for this must be implemented.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class RetrievalPipeline(val query: Operator<Retrieved>)