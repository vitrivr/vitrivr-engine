package org.vitrivr.engine.core.data

import java.util.UUID

interface Persistable {

    val id: UUID
    val transient: Boolean

}