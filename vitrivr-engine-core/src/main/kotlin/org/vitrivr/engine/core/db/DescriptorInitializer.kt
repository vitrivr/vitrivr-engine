package org.vitrivr.engine.core.db

import org.vitrivr.engine.core.data.descriptor.Descriptor

interface DescriptorInitializer<T : Descriptor> : PersistableInitializer<T> {
}