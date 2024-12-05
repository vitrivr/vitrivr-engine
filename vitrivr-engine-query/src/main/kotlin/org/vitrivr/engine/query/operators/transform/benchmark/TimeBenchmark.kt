package org.vitrivr.engine.query.operators.transform.benchmark

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.Timer
import javax.management.Descriptor

/**
 * Appends [Descriptor] to a [Retrieved] based on the values of a [Schema.Field], if available.
 *
 * @version 1.1.2
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class TimeBenchmark(
    override val input: Operator<out Retrievable>,
    val path: Path,
    val pretty: String,
    override val name: String
) : Transformer {

    companion object {
        @Volatile
        private var bl: BenchmarkLogger? = null
    }

    init {
        if (bl == null) {
            bl = BenchmarkLogger(path)
            Thread(bl).start()
        }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        val inputRetrieved = input.toFlow(scope).toList()
        bl!! log BenchmarkMessage(name, pretty, LocalDateTime.now().toString(), inputRetrieved.size)
        inputRetrieved.forEach { emit(it) }
    }
}


