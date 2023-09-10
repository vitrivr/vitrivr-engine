package org.vitrivr.engine.core.model.database.retrievable

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import java.util.*

/**
 * A [Retrievable] that has been generated as part of the retrieval process.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Retrieved : Retrievable {
    @JvmRecord
    data class Default(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrieved

    @JvmRecord
    data class WithDescriptor(override val id: UUID, override val type: String?, override val descriptors: List<Descriptor>, override val transient: Boolean) : Retrieved, RetrievableWithDescriptor

    @JvmRecord
    data class WithScore(override val id: UUID, override val type: String?, override val score: Float, override val transient: Boolean) : Retrieved, RetrievableWithScore {
        init {
            require(this.score in 0.0f..1.0f) { "Score must be between 0.0 and 1.0." }
        }
    }

    @JvmRecord
    data class WithScoreAndDescriptor(override val id: UUID, override val type: String?, override val score: Float, override val descriptors: List<Descriptor>, override val transient: Boolean) : Retrieved, RetrievableWithScore,
        RetrievableWithDescriptor {
        init {
            require(this.score in 0.0f..1.0f) { "Score must be between 0.0 and 1.0." }
        }
    }

    @JvmRecord
    data class WithDistance(override val id: UUID, override val type: String?, override val distance: Float, override val transient: Boolean) : Retrieved, RetrievableWithDistance {
        init {
            require(this.distance >= 0.0f) { "Distance must be greater or equal to zero." }
        }
    }

    @JvmRecord
    data class WithDistanceAndDescriptor(override val id: UUID, override val type: String?, override val distance: Float, override val descriptors: List<Descriptor>, override val transient: Boolean) : Retrieved, RetrievableWithDistance,
        RetrievableWithDescriptor {
        init {
            require(this.distance >= 0.0f) { "Distance must be greater or equal to zero." }
        }
    }
}