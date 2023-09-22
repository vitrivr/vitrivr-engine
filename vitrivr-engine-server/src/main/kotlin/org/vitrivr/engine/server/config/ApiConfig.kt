package org.vitrivr.engine.server.config

import kotlinx.serialization.Serializable

/**
 * Configuration regarding the RESTful API.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class ApiConfig(
    /** Port used by the RESTful API. */
    val port: Int = 7070,

    /** Flag indicating, if retrieval functionality should be exposed via RESTful API. */
    val retrieval: Boolean = true,

    /** Flag indicating, if indexing functionality should be exposed via RESTful API. */
    val index: Boolean = true
)