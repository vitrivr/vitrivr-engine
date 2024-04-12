package org.vitrivr.engine.core.model.retrievable

/**
 * A [Ingested] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
open class Ingested(
    override val id: RetrievableId,
    override val type: String?,
    override val transient: Boolean,
)  : AbstractRetrievable(id, type, transient)