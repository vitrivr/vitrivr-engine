package org.vitrivr.engine.query.operators.transform.benchmark

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import kotlin.io.path.Path

class TimeBenchmarkFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, schema: Schema, properties: Map<String, String>): TimeBenchmark {
        val logfilePath = Path(properties["logfile"]?.toString() ?: "benchmark.log")
        val prettyName = properties["pretty"]?.toString() ?: name
        return TimeBenchmark(input, logfilePath, prettyName, name)
    }
}
