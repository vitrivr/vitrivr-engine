package org.vitrivr.engine.index.util.boundaryFile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration

@Serializable
data class ShotBoundaryDetectionDescriptor(
    @SerialName("success") var success: Boolean? = null,
    @SerialName("data") var data: Data? = Data(),
    @SerialName("message") var message: String? = null
): MediaSegmentDecriptable{

        override fun toMediaSegmentDescriptors(): List<MediaSegmentDescriptor> {
            val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()
            this.data?.job?.shotBoundaries?.forEachIndexed { index, shotBoundary ->
                mediaSegementDescriptors.add(
                    MediaSegmentDescriptor(
                        this.data?.job?.collectiveId ?: "",
                        this.data?.job?.jobs?.get(index) ?: "",
                        index,
                        start = shotBoundary[1],
                        end = shotBoundary[0],
                        exists = true,
                        startAbs = Duration.ofSeconds(shotBoundary[1].toLong()),
                        endAbs = Duration.ofSeconds(shotBoundary[1].toLong()),
                    )
                )
            }
            return mediaSegementDescriptors
        }

}

@Serializable
data class Data(
    @SerialName("job") var job: Job? = Job(),
    @SerialName("queue_position") var queuePosition: String? = null,
    @SerialName("queue_length") var queueLength: Int? = null,
    @SerialName("queue_paused_until") var queuePausedUntil: String? = null,
    @SerialName("average_processing_time_s") var averageProcessingTimeS: Double? = null
)

@Serializable
data class Job(
    @SerialName("collective_id") var collectiveId: String? = null,
    @SerialName("is_complete") var isComplete: Boolean? = null,
    @SerialName("file_name") var fileName: String? = null,
    @SerialName("file_size") var fileSize: String? = null,
    @SerialName("video_duration_s") var videoDurationS: Double? = null,
    @SerialName("timestamp") var timestamp: String? = null,
    @SerialName("jobs") var jobs: ArrayList<String> = arrayListOf(),
    @SerialName("status") var status: String? = null,
    @SerialName("shot_boundaries") var shotBoundaries: ArrayList<ArrayList<Int>> = arrayListOf()
)
