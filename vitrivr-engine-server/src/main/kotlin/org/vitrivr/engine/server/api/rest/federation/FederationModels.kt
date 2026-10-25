package org.vitrivr.engine.server.api.rest.federation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Exactly one time form: timeMs, or the half-open interval [startMs, endMs). */
@Serializable
data class FederationLookupRequest(
    val source: String,
    val day: String,
    val video: String,
    val sourceType: String? = null,
    val timeMs: Long? = null,
    val startMs: Long? = null,
    val endMs: Long? = null,
    val limit: Int = 1000,
    val recordingField: String = "recording",
    val fileField: String = "file",
    val timeField: String = "time"
) {
    fun validate() {
        require(source.isNotBlank() && day.isNotBlank() && video.isNotBlank()) { "source, day and video are required." }
        require(sourceType == null || sourceType in setOf("person", "room", "unknown")) { "sourceType must be person, room or unknown." }
        require(limit in 1..1000) { "limit must be between 1 and 1000." }
        require((timeMs != null && startMs == null && endMs == null) ||
            (timeMs == null && startMs != null && endMs != null)) { "Provide timeMs OR both startMs and endMs." }
        val values = listOfNotNull(timeMs, startMs, endMs)
        require(values.all { it in 0..Long.MAX_VALUE / 1_000_000L }) { "Times must be non-negative integer milliseconds within nanosecond range." }
        if (startMs != null && endMs != null) require(endMs > startMs) { "endMs must be greater than startMs." }
    }
}

@Serializable
data class FederationSegmentMatch(
    val retrievableId: String,
    val videoPath: String,
    val startMs: Double,
    val endMs: Double,
    /** Zero for a point request or a stored timepoint; duration for an interval overlap. */
    val overlapMs: Double
)

@Serializable
data class FederationLookupResponse(val matches: List<FederationSegmentMatch>, val truncated: Boolean)

@Serializable
data class FederationIdentity(val label: String, val confidence: Float, val recognized: Boolean)

@Serializable
data class FederationIdentityResult(
    val retrievableId: String,
    /** available, no_descriptors, or not_found. No extraction-completion status is inferred. */
    val status: String,
    val storedDescriptorCount: Int,
    /** Evidence above minConfidence. The label 'unknown' is not a recognized identity. */
    val hasRecognizedPerson: Boolean,
    val requestedPersonMatched: Boolean?,
    val identities: List<FederationIdentity>
)

/** Reads configured fields by ID without running any analysers. */
@Serializable
data class FederationFeaturesRequest(
    val retrievableIds: List<String>,
    val fields: List<String>,
    val includeVectors: Boolean = false,
    val timeField: String? = "time",
    /** Optional face identity check; does not filter ASR/OCR or raw descriptor values. */
    val person: String? = null,
    val minConfidence: Float = 0f
) {
    fun validate() {
        require(retrievableIds.size in 1..1000) { "Provide between 1 and 1000 retrievableIds." }
        require(person == null || person.isNotBlank()) { "person must not be blank." }
        require(minConfidence.isFinite() && minConfidence in 0f..1f) { "minConfidence must be between 0 and 1." }
        require(fields.size in 1..32 && fields.all { it.isNotBlank() }) { "Provide between 1 and 32 non-blank field names." }
        require(timeField == null || timeField.isNotBlank()) { "timeField must be non-blank or null." }
    }
}

@Serializable
data class FederationFeatureDescriptor(
    val descriptorId: String,
    val values: JsonObject,
    /** Vector attributes omitted unless includeVectors is true. */
    val omittedAttributes: List<String>
)

@Serializable
data class FederationFeatureField(
    val status: String,
    val descriptors: List<FederationFeatureDescriptor>,
    /** Stored recognition evidence, present only for face identity fields. */
    val identity: FederationIdentityResult? = null
)

@Serializable
data class FederationTimeRange(val startMs: Double, val endMs: Double)

@Serializable
data class FederationFeatureResult(
    val retrievableId: String,
    /** available or not_found; individual fields report no_descriptors separately. */
    val status: String,
    val timeRanges: List<FederationTimeRange>,
    val fields: Map<String, FederationFeatureField>
)

@Serializable
data class FederationFeaturesResponse(val results: List<FederationFeatureResult>)
