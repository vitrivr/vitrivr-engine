package org.vitrivr.engine.core.database.blackhole.descriptors

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorInitializer] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeDescriptorInitializer<T: Descriptor<*>>(private val connection: BlackholeConnection, override val field: Schema.Field<*, T>): DescriptorInitializer<T> {
    override fun initialize() = this.connection.logIf("Initializing descriptor entity '${this.field.fieldName}'.")
    override fun deinitialize() = this.connection.logIf("De-initializing descriptor entity '${this.field.fieldName}'.")
    override fun isInitialized(): Boolean = false
    override fun truncate() = this.connection.logIf("Truncating descriptor entity '${this.field.fieldName}'.")
}