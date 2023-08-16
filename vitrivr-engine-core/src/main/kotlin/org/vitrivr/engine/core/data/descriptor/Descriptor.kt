package org.vitrivr.engine.core.data.descriptor

import org.vitrivr.engine.core.data.Persistable
import org.vitrivr.engine.core.describe.DescriberId

interface Descriptor : Persistable {

    val describerId: DescriberId

}