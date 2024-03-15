package org.vitrivr.engine.core.model.retrievable

import java.util.*

/**
 * A [Retrievable] that has been generated as part of the retrieval process.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
data class Retrieved(override val id: UUID, override val type: String?, override val transient: Boolean) :
    AbstractRetrievable(id, type, transient) {
    constructor(retrievable: Retrievable) : this(retrievable.id, retrievable.type, retrievable.transient) {
        retrievable.attributes.forEach { this.addAttribute(it) }
    }
}