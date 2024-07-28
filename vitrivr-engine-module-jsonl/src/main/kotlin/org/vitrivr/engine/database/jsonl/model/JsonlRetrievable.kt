package org.vitrivr.engine.database.jsonl.model

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved

@Serializable
data class JsonlRetrievable(val id: RetrievableId, val type: String) {
    fun toRetrieved(): Retrieved = Retrieved(id, if (type.isEmpty()) null else type, false)

    constructor(retrievable: Retrievable) : this(retrievable.id, retrievable.type ?: "")
}
