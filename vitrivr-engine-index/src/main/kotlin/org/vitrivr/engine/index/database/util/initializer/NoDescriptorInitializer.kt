package org.vitrivr.engine.index.database.util.initializer

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema

class NoDescriptorInitializer<D: Descriptor>(override val field: Schema.Field<*,D>) : DescriptorInitializer<D>, NoInitializer<D>() {
}