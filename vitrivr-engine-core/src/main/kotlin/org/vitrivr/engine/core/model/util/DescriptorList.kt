package org.vitrivr.engine.core.model.util

import org.vitrivr.engine.core.model.database.descriptor.Descriptor

class DescriptorList<D : Descriptor>(elements: Collection<D>) : ArrayList<D>(elements.size) {

    constructor(element: D): this(listOf(element))

    init {
        addAll(elements)
    }

}

fun <D : Descriptor> Collection<D>.toDescriptorList() = DescriptorList(this)