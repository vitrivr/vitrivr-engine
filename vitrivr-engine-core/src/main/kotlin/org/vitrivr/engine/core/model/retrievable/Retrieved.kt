package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.decorators.*
import java.util.*
import javax.management.relation.Relation

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

    interface RetrievedWithRelationship : Retrieved, RetrievableWithRelationship

    interface RetrievedWithProperties: Retrieved, RetrievableWithProperties

    data class Default(override val id: UUID, override val type: String?, override val transient: Boolean) : Retrieved


    data class WithDescriptor(
        override val id: UUID,
        override val type: String?,
        override val descriptors: List<Descriptor>,
        override val transient: Boolean
    ) : RetrievedWithDescriptor


    data class WithScore(
        override val id: UUID,
        override val type: String?,
        override val score: Float,
        override val transient: Boolean
    ) : RetrievedWithScore {
        init {
            require(this.score in 0.0f..1.0f) { "Score must be between 0.0 and 1.0." }
        }
    }


    data class WithScoreAndDescriptor(
        override val id: UUID,
        override val type: String?,
        override val score: Float,
        override val descriptors: List<Descriptor>,
        override val transient: Boolean
    ) : RetrievedWithScore,
        RetrievedWithDescriptor {
        init {
            require(this.score in 0.0f..1.0f) { "Score must be between 0.0 and 1.0." }
        }
    }


    data class WithDistance(
        override val id: UUID,
        override val type: String?,
        override val distance: Float,
        override val transient: Boolean
    ) : RetrievedWithDistance {
        init {
            require(this.distance >= 0.0f) { "Distance must be greater or equal to zero." }
        }
    }


    data class WithDistanceAndDescriptor(
        override val id: UUID,
        override val type: String?,
        override val distance: Float,
        override val descriptors: List<Descriptor>,
        override val transient: Boolean
    ) : RetrievedWithDistance,
        RetrievedWithDescriptor {
        init {
            require(this.distance >= 0.0f) { "Distance must be greater or equal to zero." }
        }
    }

    data class WithRelationship(
        override val id: UUID, override val type: String?,
        override val transient: Boolean,
        override val relationships: Set<Relationship> = mutableSetOf()
    ) : RetrievedWithRelationship {
        constructor(retrievable: Retrievable) : this(retrievable.id, retrievable.type, retrievable.transient)
    }


    companion object {
        fun PlusRelationship(retrieved: Retrieved, relationships: Set<Relationship> = mutableSetOf()) =
            when(retrieved) {
                is RetrievedWithRelationship -> retrieved
                is ScorePlusProperties -> ScorePlusRelationshipPlusProperties(retrieved, retrieved.properties, relationships)
                is RetrievedWithScore -> ScorePlusRelationship(retrieved, relationships)
                else -> RetrievedPlusRelationship(retrieved, relationships)
            }

        fun PlusScore(retrieved: Retrieved, score: Float = 0f) = when(retrieved) {
            is RetrievedWithScore -> retrieved
            is RetrievedWithDistance -> DistancePlusScore(retrieved, score)
            else -> RetrievedPlusScore(retrieved, score)
        }

        fun PlusProperties(retrieved: Retrieved, properties: Map<String, String> = mutableMapOf()) = when(retrieved) {
            is RetrievedWithProperties -> retrieved
            is ScorePlusRelationship -> ScorePlusRelationshipPlusProperties(retrieved, properties, retrieved.relationships)
            is RetrievedWithRelationship -> RetrievedPlusRelationshipPlusProperties(retrieved, properties)
            is RetrievedWithScore -> ScorePlusProperties(retrieved)


            //TODO other combinations

            else -> RetrievedPlusProperties(retrieved, properties)
        }
    }

    data class RetrievedPlusScore(private val retrieved: Retrieved, override val score: Float) : Retrieved by retrieved, RetrievedWithScore

    data class DistancePlusScore(private val retrievedWithDistance: RetrievedWithDistance, override val score: Float) :
        RetrievedWithDistance by retrievedWithDistance, RetrievedWithScore {
        constructor(
            retrievedWithDistance: RetrievedWithDistance,
            scoringFunction: (RetrievedWithDistance) -> Float
        ) : this(retrievedWithDistance, scoringFunction(retrievedWithDistance))
    }

    open class RetrievedPlusRelationship(
        retrieved: Retrieved,
        override val relationships: Set<Relationship> = mutableSetOf()
    ) : Retrieved by retrieved, RetrievedWithRelationship {
        override fun toString(): String {
            return "RetrievedPlusRelationship(relationships=$relationships)"
        }
    }

    class RetrievedPlusRelationshipPlusProperties(
        retrieved: RetrievedWithRelationship,
        override val properties: Map<String, String> = mutableMapOf()
    ) : RetrievedPlusRelationship(retrieved, retrieved.relationships), RetrievedWithProperties {
        override fun toString(): String {
            return "RetrievedPlusRelationshipPlusProperties(properties=$properties)"
        }
    }

    open class ScorePlusRelationship(
        retrieved: RetrievedWithScore,
        override val relationships: Set<Relationship> = mutableSetOf()
    ) : RetrievedWithScore by retrieved, RetrievedWithRelationship {
        override fun toString(): String {
            return "ScorePlusRelationship(relationships=$relationships)"
        }
    }

    class ScorePlusRelationshipPlusProperties(
        retrieved: RetrievedWithScore,
        override val properties: Map<String, String> = mutableMapOf(),
        override val relationships: Set<Relationship> = mutableSetOf()
    ) : RetrievedWithScore by retrieved, RetrievedWithRelationship, RetrievedWithProperties {
        override fun toString(): String {
            return "ScorePlusRelationshipPlusProperties(properties=$properties, relationships=$relationships)"
        }
    }

    class ScorePlusProperties(
        retrieved: RetrievedWithScore,
        override val properties: Map<String, String> = mutableMapOf()
    ): RetrievedWithScore by retrieved, RetrievedWithProperties {
        override fun toString(): String {
            return "ScorePlusProperties(properties=$properties)"
        }
    }

    class RetrievedPlusProperties(
        retrieved: Retrieved,
        override val properties: Map<String, String> = mutableMapOf()
    ) : Retrieved by retrieved, RetrievedWithProperties {
        override fun toString(): String {
            return "RetrievedPlusProperties(properties=$properties)"
        }
    }




}