package org.vitrivr.engine.index.segment

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

class DescriptorSimilaritySegmenter : TransformerFactory {



    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        TODO("Not yet implemented")
    }
}