package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.proximity.ProximityQuery

/**
 * Enumeration of [Distance] functions supported by [ProximityQuery].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
enum class Distance {
    MANHATTAN,
    EUCLIDEAN,
    COSINE;
}