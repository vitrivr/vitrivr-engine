import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.operators.general.ExporterFactory
import org.vitrivr.engine.core.source.file.MimeType


private val logger: KLogger = KotlinLogging.logger {}

class StatisticsExporter : ExporterFactory {


    override fun newExporter(name: String, input: Operator<Retrievable>, context: IndexContext): Exporter {
        logger.debug { "Creating new StatisticsExporter." }
        return Instance(input, context)
    }
    private class Instance(override val input: Operator<Retrievable>, private val context: IndexContext) : Exporter {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
            return this.input.toFlow(scope)
        }
    }
}