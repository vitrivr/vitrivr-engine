package org.vitrivr.engine.core.database.retrievable

/**
 * A [RetrievableInitializer] that does nothing.
 *
 * Only for testing purposes.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class NoRetrievableInitializer :  RetrievableInitializer {
    override fun initialize() { /* No op. */ }

    override fun truncate() { /* No op. */ }
}