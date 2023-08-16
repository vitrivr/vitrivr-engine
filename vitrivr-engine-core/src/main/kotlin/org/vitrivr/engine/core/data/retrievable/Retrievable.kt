package org.vitrivr.engine.core.data.retrievable

import org.vitrivr.engine.core.data.Persistable
import org.vitrivr.engine.core.data.content.Content
import org.vitrivr.engine.core.data.descriptor.Descriptor

interface Retrievable : Persistable {

    val partOf : Set<Retrievable>
    val parts : Set<Retrievable>
    val content: List<Content>
    val descriptors: List<Descriptor>


}