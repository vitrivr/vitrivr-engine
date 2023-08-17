package org.vitrivr.engine.model

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import java.util.*

class TransientIngestedRetrievable(content: Content) : IngestedRetrievable {

    override val transient: Boolean = true
    override val content: MutableList<Content> = mutableListOf()
    override val descriptors: MutableList<Descriptor> = mutableListOf()
    override val id: RetrievableId = UUID.randomUUID()
    override val partOf: Set<Retrievable> = emptySet()
    override val parts: Set<Retrievable> = emptySet()

    init {
        this.content.add(content)
    }

}