package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.model.database.descriptor.Descriptor

/** The [DescriberId] consists of two parts: the typeName and the instanceName. */
typealias DescriberId = Pair<String,String>

/**
 * A [Describer] that derives [Descriptor]s for the purpose of indexing or retrieval (or both).
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Describer {

    /** The ID identifying this [Describer]. Used internally, to connect [Descriptor] with orchestration and persistence layer. */
    val id: DescriberId
        get() = Pair(this.typeName, this.instanceName)

    /**
     * The type name of this [Describer] instance. Is connected to the type of description derived by this [Describer] (implementation).
     *
     * By default, the implementing class's fully-qualified name is used as [typeName].
     */
    val typeName: String
        get() = this.javaClass.name

    /**
     * The instance name of this [Describer]. Is connected to a particular instance derived by this [Describer], e.g., in case
     * a particular type of [Describer] is used with different configurations.
     *
     * The [instanceName] is used by the  persistence layer and must be unique within an instance.
     */
    val instanceName: String
}