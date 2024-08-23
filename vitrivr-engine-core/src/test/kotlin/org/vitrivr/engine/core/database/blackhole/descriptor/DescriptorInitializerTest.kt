package org.vitrivr.engine.core.database.blackhole.descriptor

import org.junit.jupiter.api.Disabled
import org.vitrivr.engine.core.database.descriptor.AbstractDescriptorInitializerTest


@Disabled("Blackhole database connect tests are skipped.")
class DescriptorInitializerTest : AbstractDescriptorInitializerTest("no-schema.json")