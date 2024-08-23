package org.vitrivr.engine.core.database.blackhole.descriptor.struct

import org.junit.jupiter.api.Disabled
import org.vitrivr.engine.core.database.descriptor.struct.AbstractFileMetadataDescriptorReaderTest

@Disabled("Blackhole database connect tests are skipped.")
class FileMetadataDescriptorReaderTest : AbstractFileMetadataDescriptorReaderTest("no-schema.json")