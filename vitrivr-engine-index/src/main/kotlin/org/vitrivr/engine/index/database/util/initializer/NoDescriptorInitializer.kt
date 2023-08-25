package org.vitrivr.engine.index.database.util.initializer

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

class NoDescriptorInitializer<T: Descriptor>(override val field: Schema.Field<T>) : DescriptorInitializer<T>, NoInitializer<T>() {
}