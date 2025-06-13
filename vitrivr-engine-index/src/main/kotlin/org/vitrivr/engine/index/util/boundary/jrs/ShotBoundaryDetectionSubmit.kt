package org.vitrivr.engine.index.util.boundary.jrs


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


/**
 * Data class for the data class ShotBoundaryDetectionSubmit(
 *
 * TODO: remove, XReeco specific
 * @version 1.0.0
 */
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