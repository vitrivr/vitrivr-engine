package org.vitrivr.engine.index.util.boundary.jrs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.index.util.boundary.MediaSegmentDecriptable
import org.vitrivr.engine.index.util.boundary.MediaSegmentDescriptor
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList


/**
 * Data class for the ShotBoundaryDetectionDescriptor.
 * TODO: remove, XReeco specific
 * @version 1.0.0
 */
@Serializable
data class ShotBoundaryDetectionDescriptor(
    @SerialName("success" ) var success : Boolean? = null,
    @SerialName("data"    ) var data    : Data?    = Data(),
    @SerialName("message" ) var message : String?  = null
): MediaSegmentDecriptable {
        override fun toMediaSegmentDescriptors(): List<MediaSegmentDescriptor> {
            val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()
            this.data?.job?.shotBoundaries?.forEachIndexed { index, shotBoundary ->
                mediaSegementDescriptors.add(
                    MediaSegmentDescriptor(
                        this.data?.job?.jobId?:"",
                        UUID.randomUUID().toString(),
                        index,
                        start = shotBoundary[0].toInt(),
                        end = shotBoundary[1].toInt(),
                        exists = true,
                        startAbs = Duration.ofSeconds(shotBoundary[0].toLong()),
                        endAbs = Duration.ofSeconds(shotBoundary[1].toLong()),
                    )
                )
            }
            return mediaSegementDescriptors
        }
}

@Serializable
data class Data(
    @SerialName("job"            ) var job           : Job?    = Job(),
    @SerialName("queue_position" ) var queuePosition : String? = null,
    @SerialName("queue_length"   ) var queueLength   : Int?    = null
)

@Serializable
data class Job(
    @SerialName("job_id"           ) var jobId          : String?                   = null,
    @SerialName("status"           ) var status         : String?                   = null,
    @SerialName("file_name"        ) var fileName       : String?                   = null,
    @SerialName("file_size"        ) var fileSize       : String?                   = null,
    @SerialName("video_duration_s" ) var videoDurationS : Double?                   = null,
    @SerialName("timestamp"        ) var timestamp      : String?                   = null,
    @SerialName("shot_boundaries"  ) var shotBoundaries : ArrayList<ArrayList<Float>> = arrayListOf()
)
