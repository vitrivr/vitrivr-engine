package org.vitrivr.engine.core.model

/**
 * A [Persistable] object that can be stored using the persistence layer.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Persistable {
    /**
     * Flag indicating, whether this [Persistable] is transient or persistent. The functionality of this flag is two-fold.
     *
     * - It indicates that a [Persistable] should (or shouldn't) be persisted (mostly during extraction).
     * - It indicates that a [Persistable] is backed by a persistent entry (mostly during indexing).
     */
    val transient: Boolean
}