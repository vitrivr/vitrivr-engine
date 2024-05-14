import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class StatisticsConsumer {
    /** The [ExecutorService] used to execution [] */
    private val executor: ExecutorService = Executors.newCachedThreadPool()


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun produceNumbers(channel: Channel<Int>) {
        while (!channel.isClosedForSend) {
            (500..2000).random().let {
                println("Sending $it")
                channel.send(it)
                suspend { delay(it.toLong()) }
            }

        }
        channel.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun consumeNumbers(channel: Channel<Int>) {
        var millis = System.currentTimeMillis()
        while (!channel.isClosedForReceive) {

            channel.receive().let { num ->
                System.currentTimeMillis().let {
                    println("Received $num current time: ${it - millis}")
                    millis = it
                }

            }
        }
    }

    fun main() = runBlocking<Unit> {
        val channel = Channel<Int>()
        launch { produceNumbers(channel) }
        consumeNumbers(channel)
    }




}