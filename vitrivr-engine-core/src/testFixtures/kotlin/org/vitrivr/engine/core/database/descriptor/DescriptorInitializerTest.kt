package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.descriptor.AbstractDescriptorInitializerTest
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * An [AbstractDescriptorInitializerTest] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class DescriptorInitializerTest(schemaPath: String) : AbstractDescriptorInitializerTest(schemaPath)