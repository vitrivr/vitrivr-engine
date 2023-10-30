package org.vitrivr.engine.core.source

import java.io.InputStream
import java.util.*

typealias SourceId = UUID

/**
 * A [Source] of data that can be used in an extraction pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Source {
    /** Unique [SourceId] for this [Source]. */
    val sourceId: SourceId

    /** The name of the [Source]. */
    val name: String

    /** The [MediaType] of the [Source]. */
    val type: MediaType

    /** The timestamp at which this [Source] was generated. */
    val timestamp: Long

    /** Metadata associated with this [Source]. */
    val metadata: Map<String, Any>

    /**
     * Opens a [InputStream] for this [Source]. It remains up to the caller to open the [InputStream].
     *
     * @return [InputStream] for this [Source]
     */
    fun newInputStream(): InputStream
}