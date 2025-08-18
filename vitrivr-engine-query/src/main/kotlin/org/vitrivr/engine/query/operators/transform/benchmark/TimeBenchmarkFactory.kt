package org.vitrivr.engine.query.operators.transform.benchmark

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import kotlin.io.path.Path

class TimeBenchmarkFactory() : OperatorFactory {
    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): TimeBenchmark {
        require(context is Context)
        val logfilePath = Path(context[name, "logfile"]?.toString() ?: "benchmark.log")
        val prettyName = context[name, "pretty"]?.toString() ?: name
        return TimeBenchmark(inputs.values.first(), logfilePath, prettyName, name)
    }
}
