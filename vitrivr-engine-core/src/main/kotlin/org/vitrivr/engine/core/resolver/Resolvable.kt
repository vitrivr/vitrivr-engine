package org.vitrivr.engine.core.resolver

import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.source.file.MimeType
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * A [Resolvable] as returned by a [Resolver].
 *
 * @author Fynn Faber
 * @version 1.0.0.
 */
interface Resolvable {
    /** The [RetrievableId] represented by this [Resolvable]. */
    val retrievableId: RetrievableId

    /** The [MimeType] of the data represented by this [Resolvable]. */
    val mimeType: MimeType

    /**
     * Returns the URI of the resource (e.g. disk path, web uri, ) represented by this [Resolvable].
     *
     * @return URI of the resource.
     */
    val path: Path


    /**
     * Returns true, if the resource for this [Resolvable] exists already.
     */
    fun exists(): Boolean

    /**
     * Opens an [InputStream] for this  [Resolvable].
     *
     * It is up to the caller to close this [InputStream]!
     *
     * @return New [InputStream] instance.
     */
    fun openInputStream(): InputStream

    /**
     * Opens an [OutputStream] for this [Resolvable].
     *
     * It is up to the caller to close this [OutputStream]!
     *
     * @return New [OutputStream] instance.
     */
    fun openOutputStream(): OutputStream


}
