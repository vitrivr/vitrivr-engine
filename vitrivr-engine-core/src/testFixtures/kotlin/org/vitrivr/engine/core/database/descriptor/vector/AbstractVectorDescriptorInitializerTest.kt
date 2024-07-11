package org.vitrivr.engine.core.database.descriptor.vector

import org.vitrivr.engine.core.database.descriptor.AbstractDescriptorInitializerTest
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractVectorDescriptorInitializerTest(schemaPath: String) : AbstractDescriptorInitializerTest(schemaPath) {
    /** The [Schema.Field] used for this [AbstractVectorDescriptorInitializerTest]. */
    override val field: Schema.Field<*, *> = this.testSchema["averagecolor"]!!
}