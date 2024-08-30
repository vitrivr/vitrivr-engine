package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.AbstractDescriptorInitializerTest

/**
 * An [AbstractDescriptorInitializerTest] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class DescriptorInitializerTest : AbstractDescriptorInitializerTest("test-schema-postgres.json")