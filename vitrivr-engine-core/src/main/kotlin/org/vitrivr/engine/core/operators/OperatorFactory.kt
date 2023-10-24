package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.Source
import kotlin.reflect.jvm.internal.impl.incremental.components.ScopeKind

interface OperatorFactory <I : Operator<*>, O : Operator<*>> {
    fun  newOperator(input: I, parameters: Map<String,Any> = emptyMap(), schema: Schema, context: Context) : O
}