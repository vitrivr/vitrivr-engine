package org.vitrivr.engine.core.source

/**
 * A collection of key names for metadata that is used by the [Source] implementations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object Metadata {
    const val METADATA_KEY_IMAGE_WIDTH = "image.width"
    const val METADATA_KEY_IMAGE_HEIGHT = "image.height"
    const val METADATA_KEY_VIDEO_BITRATE = "video.bitrate"
    const val METADATA_KEY_AV_DURATION = "av.duration"
    const val METADATA_KEY_VIDEO_FPS = "video.fps"
    const val METADATA_KEY_AUDIO_CHANNELS = "audio.channels"
    const val METADATA_KEY_AUDIO_SAMPLERATE = "audio.samplerate"
    const val METADATA_KEY_AUDIO_SAMPLESIZE = "audio.samplesize"
    const val METADATA_KEY_AUDIO_BITRATE = "audio.bitrate"
}
