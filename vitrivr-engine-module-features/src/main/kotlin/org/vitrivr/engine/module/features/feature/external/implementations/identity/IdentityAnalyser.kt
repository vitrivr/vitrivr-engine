package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.features.bool.StructBooleanRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.struct.FaceIdentityDescriptor
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.query.basics.Distance
import java.util.UUID

abstract class IdentityAnalyser : ExternalAnalyser<ImageContent, FaceIdentityDescriptor>() {
    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = FaceIdentityDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) =
        FaceIdentityDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", 0f)

    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, FaceIdentityDescriptor>, query: Query, context: Context
    ) = when (query) {
        is BooleanQuery -> {
            val normalized = if (query is SimpleBooleanQuery<*> && query.attributeName == FaceIdentityDescriptor.LABEL_FIELD_NAME && query.value is Value.String) {
                SimpleBooleanQuery(Value.String(FaceIdentityDescriptor.normalizeLabel(query.value.value as String)),
                    query.comparison, query.attributeName, query.limit)
            } else query
            StructBooleanRetriever(field, normalized, context)
        }
        is ProximityQuery<*> -> {
            val vector = query.value as? Value.FloatVector
            require(vector != null && vector.value.size == FaceIdentityDescriptor.DIMENSIONS && vector.value.all { it.isFinite() }) {
                "Face queries require a 512-dimensional float vector."
            }
            require(query.attributeName == null || query.attributeName == FaceIdentityDescriptor.VECTOR_FIELD_NAME) {
                "Face proximity queries must target the vector attribute."
            }
            IdentityRetriever(field, query.copy(attributeName = FaceIdentityDescriptor.VECTOR_FIELD_NAME), context)
        }
        else -> throw IllegalArgumentException("Face identities support boolean and proximity queries.")
    }

    /** All three fields use stateless AdaFace extraction for query images. */
    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, FaceIdentityDescriptor>,
        content: Map<String, ImageContent>,
        context: Context
    ): AbstractRetriever<ImageContent, FaceIdentityDescriptor> {
        require(content.size == 1) { "Face retrieval requires exactly one query image or label." }
        val queryContent: ContentElement<*> = (content as Map<String, ContentElement<*>>).values.single()
        if (queryContent is TextContent) {
            val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
            return newRetrieverForQuery(field, SimpleBooleanQuery(Value.String(queryContent.text),
                attributeName = FaceIdentityDescriptor.LABEL_FIELD_NAME, limit = limit), context)
        }
        require(queryContent is ImageContent) { "Face retrieval supports image or text label content." }
        val host = context.getProperty(field.fieldName, HOST_PARAMETER_NAME)
            ?: field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val faces = AdaFace.analyse(queryContent, host, identify = false)
            .filter { it.vector != null }
        require(faces.isNotEmpty()) { "No face was detected in the query image." }
        val faceIndexParameter = context.getProperty(field.fieldName, "faceIndex")
        require(faces.size == 1 || faceIndexParameter != null) {
            "Multiple faces were detected. Set faceIndex to select a face (zero-based detection order)."
        }
        val faceIndex = if (faceIndexParameter == null) 0 else faceIndexParameter.toIntOrNull()
            ?: throw IllegalArgumentException("faceIndex must be an integer.")
        require(faceIndex in faces.indices) { "faceIndex must be between 0 and ${faces.lastIndex}." }
        return newRetrieverForDescriptors(field, listOf(faces[faceIndex]), context)
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, FaceIdentityDescriptor>,
        descriptors: Collection<FaceIdentityDescriptor>,
        context: Context
    ): AbstractRetriever<ImageContent, FaceIdentityDescriptor> {
        require(descriptors.size == 1) { "Face retrieval requires exactly one query descriptor." }
        /* API inputs arrive as generic string/vector descriptors through Schema.Field. */
        val descriptor: Descriptor<*> = (descriptors as Collection<Descriptor<*>>).single()
        val vector = when (descriptor) {
            is FaceIdentityDescriptor -> descriptor.vector
            is FloatVectorDescriptor -> descriptor.vector
            is StringDescriptor -> null
            else -> throw IllegalArgumentException("Face retrieval requires a face, float vector, or string descriptor.")
        }
        val label = when (descriptor) {
            is FaceIdentityDescriptor -> descriptor.label
            is StringDescriptor -> descriptor.value
            else -> null
        }
        val distance = context.getProperty(field.fieldName, "distance")?.let { Distance.valueOf(it.uppercase()) } ?: Distance.COSINE
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        require(limit > 0) { "The retrieval limit must be positive." }
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
        return newRetrieverForQuery(field,
            vector?.let { ProximityQuery(it, distance = distance, k = limit, fetchVector = fetchVector,
                attributeName = FaceIdentityDescriptor.VECTOR_FIELD_NAME) }
                ?: SimpleBooleanQuery(requireNotNull(label), attributeName = FaceIdentityDescriptor.LABEL_FIELD_NAME, limit = limit),
            context
        )
    }
}
