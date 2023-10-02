package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.TransformerFactory

class DummyTransformerFactory : TransformerFactory {

    var input: Operator<ContentElement<*>>? = null

    override fun setSource(source: Operator<ContentElement<*>>) : DummyTransformerFactory {
        this.input = source
        return this
    }

    override fun createOperator(): DummyTransformer {
        return DummyTransformer(this.input!!)
    }
}