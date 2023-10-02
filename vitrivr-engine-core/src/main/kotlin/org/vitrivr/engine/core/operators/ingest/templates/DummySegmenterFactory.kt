package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory

class DummySegmenterFactory : SegmenterFactory {


    var input: Operator<ContentElement<*>>? = null

    override fun setSource(source: Operator<ContentElement<*>>) : DummySegmenterFactory {
        this.input = source
        return this
    }

    override fun createOperator(): DummySegmenter {
        return DummySegmenter(this.input!!)
    }
}