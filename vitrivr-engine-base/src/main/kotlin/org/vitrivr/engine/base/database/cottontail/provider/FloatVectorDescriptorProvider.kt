package org.vitrivr.engine.base.database.cottontail.provider

import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.initializer.FloatVectorDescriptorInitializer
import org.vitrivr.engine.base.database.cottontail.reader.FloatVectorDescriptorReader
import org.vitrivr.engine.base.database.cottontail.writer.FloatVectorDescriptorWriter
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.operators.Describer

/**
 * A [DescriptorProvider] for [FloatVectorDescriptor].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class FloatVectorDescriptorProvider(private val connection: CottontailConnection): DescriptorProvider<FloatVectorDescriptor> {
    override fun newInitializer(describer: Describer<FloatVectorDescriptor>) = FloatVectorDescriptorInitializer(describer, this.connection)
    override fun newReader(describer: Describer<FloatVectorDescriptor>) = FloatVectorDescriptorReader(describer, this.connection)
    override fun newWriter(describer: Describer<FloatVectorDescriptor>) = FloatVectorDescriptorWriter(describer, this.connection)
}