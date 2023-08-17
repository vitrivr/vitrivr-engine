package org.vitrivr.engine.core.source

import java.io.Closeable
import java.io.InputStream

/**
 * A [Source] of data that can be used in an extraction pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Source: Closeable {
    /** The name of the [Source]. */
    val name: String

    /** The [MediaType] of the [Source]. */
    val type: MediaType

    /** The [InputStream] that provides access to the [Source]s* data. */
    val inputStream: InputStream

    /** The timestamp at which this [Source] was generated. */
    val timestamp: Long

    /** Metadata associated with this [Source]. */
    val metadata: Map<String, Any>

    /**
     * Closes the [InputStream] associated with this [Source]-
     */
    override fun close() {
        this.inputStream.close()
    }
}