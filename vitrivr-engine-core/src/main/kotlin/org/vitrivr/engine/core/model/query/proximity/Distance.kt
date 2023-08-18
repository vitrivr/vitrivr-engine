package org.vitrivr.engine.core.model.query.proximity

/**
 * Enumeration of [Distance] functions supported by [ProximityQuery].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
enum class Distance {
    MANHATTAN,
    EUCLIDEAN,
    COSINE
}