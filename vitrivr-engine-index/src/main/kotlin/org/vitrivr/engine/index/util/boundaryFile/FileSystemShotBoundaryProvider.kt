package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import java.nio.file.Path
import java.time.Duration
import java.util.*


class FileSystemShotBoundaryProvider : ShotBoundaryProviderFactory {

    override fun newShotBoundaryProvider(
        name: String,
        parameters: Map<String, String>,
        context: IndexContext
    ): ShotBoundaryProvider {
        val boundaryFilesPath = parameters["boundaryFilesPath"]
            ?: throw IllegalArgumentException("Property 'boundaryFilesPath' must be specified")
        val boundaryFileExtension = parameters["boundaryFileExtension"] ?: ".tsv"
        val toNanoScale = parameters["toNanoScale"]?.toDouble()
            ?: throw IllegalArgumentException("Property 'toNanoScale' must be specified")
        return Instance(boundaryFilesPath, boundaryFileExtension, toNanoScale)
    }

    class Instance(
        private val boundaryFilesPath: String,
        private val fileExtension: String = ".tsv",
        private val toNanoScale: Double = 1e9
    ) : ShotBoundaryProvider {


        override fun decode(boundaryId: String): List<MediaSegmentDescriptor> {

            val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()

            with(Path.of(this.boundaryFilesPath).resolve("$boundaryId$fileExtension").toFile().bufferedReader()) {
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
                            val (startframe, starttime, endframe, endtime) = line.split(" ", "\t")
                            mediaSegementDescriptors.add(
                                MediaSegmentDescriptor(
                                    boundaryId,
                                    UUID.randomUUID().toString(),
                                    shotCounter,
                                    startframe.toInt(),
                                    endframe.toInt(),
                                    Duration.ofNanos((starttime.toDouble() * toNanoScale).toLong()),
                                    Duration.ofNanos((endtime.toDouble() * toNanoScale).toLong()),
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
}