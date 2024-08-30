package org.vitrivr.engine.core.model.retrievable

/**
 * A [Ingested] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.2.0
 */
data class Ingested(override val id: RetrievableId, override val type: String?, override val transient: Boolean) : AbstractRetrievable(id, type, transient) {

    override fun copy(): Ingested = this.copy(id = id)
}