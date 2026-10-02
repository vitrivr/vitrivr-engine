package org.vitrivr.engine.module.features.feature.external.implementations.identity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.FaceIdentityDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute

/** Scores face proximity results using the same distance correspondence as dense retrieval. */
internal class IdentityRetriever(
    field: Schema.Field<ImageContent, FaceIdentityDescriptor>, query: ProximityQuery<*>, context: Context
) : AbstractRetriever<ImageContent, FaceIdentityDescriptor>(field, query, context) {
    private val correspondence = LinearCorrespondence(context.getProperty(field.fieldName, "maxDistance")?.toFloatOrNull() ?: 2f)

    override fun toFlow(scope: CoroutineScope) = flow {
        reader.queryAndJoin(query).forEach { retrieved ->
            val distances = retrieved.filteredAttributes(DistanceAttribute::class.java)
            emit(retrieved.copy(attributes = retrieved.attributes + distances.map { correspondence(it) }))
        }
    }
}
