package org.vitrivr.engine.core.database.blackhole.descriptor.vector

import org.junit.jupiter.api.Disabled
import org.vitrivr.engine.core.database.descriptor.vector.AbstractFloatVectorDescriptorReaderTest

@Disabled("Blackhole database connect tests are skipped.")
class FloatVectorDescriptorReaderTest : AbstractFloatVectorDescriptorReaderTest("no-schema.json")