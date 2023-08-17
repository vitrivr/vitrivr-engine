package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.database.descriptor.Descriptor

interface DescriptorInitializer<T : Descriptor> : PersistableInitializer<T> {
}