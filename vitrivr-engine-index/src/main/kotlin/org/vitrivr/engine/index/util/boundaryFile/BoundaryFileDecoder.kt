package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.model.types.Type
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.util.UUID

class BoundaryFileDecoder(private val boundaryFilesPath: Path) {


    public fun decode(videoFileName: String): List<MediaSegmentDescriptor> {

        val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()

        with(this.boundaryFilesPath.resolve("$videoFileName.tsv").toFile().bufferedReader()) {
            var shotCounter = 0
            while (true) {

                var line: String = readLine() ?: break
                line = line.trim()

                when {
                    !line[0].isDigit() -> {
                        continue
                    }

                    line.split(" ", "\t").size < 2 -> {
                        continue
                    }

                    line.split(" ", "\t").size == 4 -> {
                        val (startframe,	starttime,	endframe,	endtime) = line.split(" ", "\t")
                        mediaSegementDescriptors.add(
                            MediaSegmentDescriptor(
                                videoFileName,
                                UUID.randomUUID().toString(),
                                shotCounter,
                                startframe.toInt(),
                                endframe.toInt(),
                                Duration.ofNanos((starttime.toDouble()*1e9).toLong()),
                                Duration.ofNanos((endtime.toDouble()*1e9).toLong()),
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