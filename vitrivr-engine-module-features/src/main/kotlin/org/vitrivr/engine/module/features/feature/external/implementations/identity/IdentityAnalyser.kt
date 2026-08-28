package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import java.util.UUID

abstract class IdentityAnalyser : ExternalAnalyser<ImageContent, LabelDescriptor>() {
    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = LabelDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) =
        LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", 0f)

    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, LabelDescriptor>, query: Query, context: Context
    ): StructBooleanRetriever<ImageContent, LabelDescriptor> {
        require(query is BooleanQuery) { "The query is not a BooleanQuery." }
        return StructBooleanRetriever(field, query, context)
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, LabelDescriptor>,
        descriptors: Collection<LabelDescriptor>,
        context: Context
    ) = newRetrieverForQuery(
        field,
        SimpleBooleanQuery(descriptors.first().label, attributeName = LabelDescriptor.LABEL_FIELD_NAME),
        context
    )
}
