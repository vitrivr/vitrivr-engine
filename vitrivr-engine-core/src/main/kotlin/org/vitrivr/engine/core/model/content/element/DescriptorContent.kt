package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.descriptor.Descriptor

interface DescriptorContent<T : Descriptor<T>> : ContentElement<Descriptor<T>> {

    override val type: ContentType
        get() = ContentType.DESCRIPTOR

}