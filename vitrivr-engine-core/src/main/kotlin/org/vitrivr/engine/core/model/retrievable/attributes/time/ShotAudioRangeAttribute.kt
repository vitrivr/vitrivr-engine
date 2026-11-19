package org.vitrivr.engine.core.model.retrievable.attributes.time

import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute

/** Half-open interval for assigning audio timestamps to a shot with inclusive video frame boundaries. */
data class ShotAudioRangeAttribute(val startNs: Long, val endExclusiveNs: Long) : RetrievableAttribute
