package org.vitrivr.engine.index.util.boundaryFile.JRS


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class ShotBoundaryDetectionSubmit(

    @SerialName("success") var success: Boolean? = null,
    @SerialName("data") var data: ShotBoundaryDetectionSubmitData? = ShotBoundaryDetectionSubmitData(),
    @SerialName("message") var message: String? = null

)


@Serializable
data class ShotBoundaryDetectionSubmitData(
    @SerialName("job_id") var jobId: String? = null
)