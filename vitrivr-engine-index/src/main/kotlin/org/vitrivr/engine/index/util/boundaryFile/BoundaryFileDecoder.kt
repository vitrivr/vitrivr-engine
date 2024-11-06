package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.model.types.Type
import java.io.File
import java.util.UUID

class BoundaryFileDecoder() {
    public fun decode(boundaryFile: String, videoId: String): List<MediaSegmentDescriptor> {
        assert(boundaryFile.isNotEmpty()) { "Boundary file must not be empty" }
        assert(videoId.isNotEmpty()) { "Video ID must not be empty" }

        val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()

        with(File(boundaryFile).bufferedReader()) {
            var shotCounter = 0
            while (true) {

                var line: String = readLine() ?: break
                line = line.trim()

                when {
                    line[0].isDigit() -> {
                        continue
                    }

                    line.split(" ").size < 2 -> {
                        continue
                    }

                    line.split(" ").size == 2 -> {
                        val (start, end) = line.split(" ")
                        mediaSegementDescriptors.add(
                            MediaSegmentDescriptor(
                                videoId,
                                UUID.randomUUID().toString(),
                                shotCounter,
                                start.toInt(),
                                end.toInt(),
                                start.toDouble(),
                                end.toDouble(),
                                true
                            )
                        )
                    }
                }
                shotCounter++
            }
        }
        return mediaSegementDescriptors
    }
}