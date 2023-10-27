package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDistance
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithScore
import java.util.*

/**
 * A [Retrievable] that has been generated as part of the retrieval process.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Retrieved : Retrievable {

    interface RetrievedWithDescriptor : Retrieved, RetrievableWithDescriptor

    interface RetrievedWithDistance : Retrieved, RetrievableWithDistance

    interface RetrievedWithScore : Retrieved, RetrievableWithScore

    data class Default(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrieved


    data class WithDescriptor(override val id: UUID, override val type: String?, override val descriptors: List<Descriptor>, override val transient: Boolean) : RetrievedWithDescriptor


    data class WithScore(override val id: UUID, override val type: String?, override val score: Float, override val transient: Boolean) : RetrievedWithScore {
        init {
            require(this.score in 0.0f..1.0f) { "Score must be between 0.0 and 1.0." }
        }
    }

    data class PlusScore(private val retrievedWithDistance: RetrievedWithDistance, override val score: Float) : RetrievedWithDistance by retrievedWithDistance, RetrievedWithScore {
        constructor(retrievedWithDistance: RetrievedWithDistance, scoringFunction: (RetrievedWithDistance) -> Float) : this(retrievedWithDistance, scoringFunction(retrievedWithDistance))
    }


    data class WithScoreAndDescriptor(override val id: UUID, override val type: String?, override val score: Float, override val descriptors: List<Descriptor>, override val transient: Boolean) : RetrievedWithScore,
        RetrievedWithDescriptor {
        init {
            require(this.score in 0.0f..1.0f) { "Score must be between 0.0 and 1.0." }
        }
    }


    data class WithDistance(override val id: UUID, override val type: String?, override val distance: Float, override val transient: Boolean) : RetrievedWithDistance {
        init {
            require(this.distance >= 0.0f) { "Distance must be greater or equal to zero." }
        }
    }


    data class WithDistanceAndDescriptor(override val id: UUID, override val type: String?, override val distance: Float, override val descriptors: List<Descriptor>, override val transient: Boolean) : RetrievedWithDistance,
        RetrievedWithDescriptor {
        init {
            require(this.distance >= 0.0f) { "Distance must be greater or equal to zero." }
        }
    }
}