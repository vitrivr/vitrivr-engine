package org.vitrivr.engine.database.cottontaildb.descriptor.struct

import org.vitrivr.engine.core.database.descriptor.struct.AbstractFileMetadataDescriptorReaderTest

/**
 * An [AbstractFileMetadataDescriptorReaderTest] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileMetadataDescriptorReaderTest : AbstractFileMetadataDescriptorReaderTest("test-schema-cottontaildb.json")