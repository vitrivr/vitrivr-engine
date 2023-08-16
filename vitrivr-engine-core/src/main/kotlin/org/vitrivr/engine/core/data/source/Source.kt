package org.vitrivr.engine.core.data.source

import java.io.InputStream

interface Source {

    val name: String
    val type: MediaType
    val inputStream: InputStream
    val timestamp: Long
    val metadata: Map<String, Any>

}