package org.vitrivr.engine.index.enumerate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.Source

class ChannelEnumerator : EnumeratorFactory{
    override fun newOperator(context: IndexContext, parameters: Map<String, String>): Enumerator {
        return Instance(context)
    }

     class Instance(private val context: IndexContext) : Enumerator {

        private var producer: ProducerScope<Source>? = null
        override fun toFlow(scope: CoroutineScope): Flow<Source> = channelFlow<Source> {
            this@Instance.producer = this
        }

        fun send(source: Source) = runBlocking {
            this@Instance.producer?.send(source)
        }
    }

}