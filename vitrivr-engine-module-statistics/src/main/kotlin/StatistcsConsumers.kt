import kotlinx.coroutines.channels.Channel

class StatistcsConsumers {
    companion object {
        val statisticsConsumers = mutableMapOf<String, StatisticsConsumer>()
    }

    fun getStatisticsSendingChannel(statisticsName: String): Channel<*> {

        if (!statisticsConsumers.containsKey(statisticsName)) {
            statisticsConsumers[statisticsName] = StatisticsConsumer()
        }
        return statisticsConsumers[statisticsName].statisticsSendingChannel
    }

}