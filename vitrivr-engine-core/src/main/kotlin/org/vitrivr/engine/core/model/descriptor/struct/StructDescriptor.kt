package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Descriptor

/**
 * A [Descriptor] that uses a complex structure.
 *
 * **Caution: Respect the constructor arguments:**
 *
 * 1. First: descriptorId
 * 2. Second: retrievableId
 * 3. Then: Custom
 * 4. Last: field
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface StructDescriptor : Descriptor
