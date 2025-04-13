package org.vitrivr.engine.module.features.feature.dominantcolor

import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.BooleanQuery


class DominantColorRetriever(field: Schema.Field<ImageContent, LabelDescriptor>, query: BooleanQuery, properties: Map<String, String>) : AbstractRetriever<ImageContent, LabelDescriptor>(field, query, properties)