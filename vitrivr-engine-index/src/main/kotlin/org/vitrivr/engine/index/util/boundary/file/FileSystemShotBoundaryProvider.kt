package org.vitrivr.engine.index.util.boundary.file

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.index.util.boundary.MediaSegmentDescriptor
import org.vitrivr.engine.index.util.boundary.ShotBoundaryProvider
import org.vitrivr.engine.index.util.boundary.ShotBoundaryProviderFactory
import java.nio.file.Path
import java.time.Duration
import java.util.UUID

/**
 * A [ShotBoundaryProvider] and its [ShotBoundaryProviderFactory] that operates based on files containing shot boundaries.
 *
 * @author Raphael Waltenspühl
 * @version 1.1.0
 */
class FileSystemShotBoundaryProvider : ShotBoundaryProviderFactory {
    /**
     * Creates a new instance of [ShotBoundaryProvider] that uses configuration parameters from the provided [Context] object
     * to initialize the boundary files path, extension, and scaling factor.
     *
     * @param name The name of the provider
     * @param context A [Context] object containing configuration parameters for the provider
     * @return An instance of [ShotBoundaryProvider] that can decode media segment boundaries from files stored in the file system
     * @throws IllegalArgumentException if required configuration parameters are missing or invalid
     */
    override fun newShotBoundaryProvider(name: String, context: Context): ShotBoundaryProvider {
        val boundaryFilesPath = context[name, "boundaryFilesPath"]
            ?: throw IllegalArgumentException("Property 'boundaryFilesPath' must be specified")
        val boundaryFileExtension = context[name, "boundaryFileExtension"] ?: ".tsv"
        val toNanoScale = context[name, "toNanoScale"]?.toDouble()
            ?: throw IllegalArgumentException("Property 'toNanoScale' must be specified")
        return Instance(boundaryFilesPath, boundaryFileExtension, toNanoScale)
    }

    /**
     * An implementation of the [ShotBoundaryProvider] interface that decodes media segment boundaries from files stored in the file system.
     *
     * @param boundaryFilesPath The path to the directory containing boundary files
     * @param fileExtension The file extension used for boundary files (default is ".tsv")
     * @param toNanoScale A scaling factor used to convert seconds to nanoseconds (default is 1e9)
     */
    class Instance(private val boundaryFilesPath: String, private val fileExtension: String = ".tsv", private val toNanoScale: Double = 1e9) : ShotBoundaryProvider {
        /**
         * Decodes media segment boundaries from a specified file and transforms it into a list of [MediaSegmentDescriptor] objects.
         *
         * @param assetUri The ID of the media segment to decode
         * @return A list of [MediaSegmentDescriptor] objects representing the decoded media segment boundaries
         */

        override fun decode(assetUri: String): List<MediaSegmentDescriptor> {
            val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()
            with(Path.of(this.boundaryFilesPath).resolve("$assetUri$fileExtension").toFile().bufferedReader()) {
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
                                    assetUri,
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