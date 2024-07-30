package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.database.descriptor.vector.AbstractFloatVectorDescriptorReaderTest

/**
 * An [AbstractFloatVectorDescriptorReaderTest] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorReaderTest : AbstractFloatVectorDescriptorReaderTest("test-schema-postgres.json")