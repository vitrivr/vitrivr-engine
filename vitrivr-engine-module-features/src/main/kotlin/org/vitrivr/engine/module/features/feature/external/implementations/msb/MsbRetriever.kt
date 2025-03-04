package org.vitrivr.engine.module.features.feature.external.implementations.msb

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.ShotBoundaryDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query

class MsbRetriever(field: Schema.Field<ContentElement<*>, ShotBoundaryDescriptor>, query: Query, context: QueryContext) : AbstractRetriever<ContentElement<*>, ShotBoundaryDescriptor>(field, query, context)
