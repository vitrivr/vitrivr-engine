package org.vitrivr.engine.core.database.blackhole.retrievable

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer

/**
 * A [RetrievableInitializer] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeRetrievableInitializer(private val connection: BlackholeConnection) : RetrievableInitializer {
    override fun initialize() = this.connection.logIf("Initializing entities 'retrievable' and 'relationship'.")
    override fun deinitialize() = this.connection.logIf("De-initializing entities 'retrievable' and 'relationship'.")
    override fun isInitialized(): Boolean = false
    override fun truncate() = this.connection.logIf("Truncating entities 'retrievable' and 'relationship'.")
}