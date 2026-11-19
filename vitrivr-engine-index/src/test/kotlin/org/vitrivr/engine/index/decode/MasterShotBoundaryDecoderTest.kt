package org.vitrivr.engine.index.decode

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import org.vitrivr.engine.core.config.ContextFactory
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import java.awt.Color
import java.nio.file.Files
import java.nio.file.Path

class MasterShotBoundaryDecoderTest {
    @TempDir
    lateinit var boundaryFiles: Path

    @Test
    fun keepsSingleFrameShotsAndOriginalBoundaries() = runBlocking {
        Files.writeString(boundaryFiles.resolve("color_test.tsv"), "startframe\tstarttime\tendframe\tendtime\n" +
            "0\t0\t0\t0\n25\t1\t25\t1\n50\t2\t50\t2\n")
        val shots = decode(Path.of("src/test/resources/videos"), boundaryFiles)
        assertEquals(listOf(0L, 1_000_000_000L, 2_000_000_000L).map { TimeRangeAttribute(it, it) }, shots.map(::range))
        for ((shot, color) in shots.zip(listOf(Color.RED, Color.GREEN, Color.BLUE))) {
            val image = shot.content.filterIsInstance<ImageContent>().single().content
            val pixel = Color(image.getRGB(image.width / 2, image.height / 2))
            assertTrue(listOf(color.red - pixel.red, color.green - pixel.green, color.blue - pixel.blue).all { kotlin.math.abs(it) < 20 })
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VITRIVR_V3C_MINI_ROOT", matches = ".+")
    fun keepsEveryV3cBoundaryAndItsAudio() = runBlocking {
        val root = Path.of(System.getenv("VITRIVR_V3C_MINI_ROOT"))
        val boundaries = root.resolve("msb_v3c")
        val expected = Files.readAllLines(boundaries.resolve("00001.tsv")).drop(1).map { line ->
            val columns = line.split('\t')
            TimeRangeAttribute((columns[1].toDouble() * 1e9).toLong(), (columns[3].toDouble() * 1e9).toLong())
        }
        val shots = decode(root.resolve("v3c"), boundaries)
        assertEquals(expected, shots.map(::range))
        assertTrue(shots.all {
            it.content.filterIsInstance<ImageContent>().size == 1 && it.content.filterIsInstance<AudioContent>().size == 1
        })
    }

    private suspend fun kotlinx.coroutines.CoroutineScope.decode(videos: Path, boundaries: Path): List<Retrievable> {
        val schema = Schema("test", Path.of("."), BlackholeConnection("test", BlackholeConnectionProvider()))
        val context = ContextFactory.newContext(schema, IngestionContextConfig("InMemoryContentFactory", emptyList())).copy(
            local = mapOf(
                "videos" to mapOf("path" to videos.toString(), "depth" to "1", "mediaTypes" to "VIDEO"),
                "decode" to mapOf("boundaryFilesPath" to boundaries.toString(), "toNanoScale" to "1e9")
            )
        )
        val input = FileSystemEnumerator().newOperator("videos", context)
        val results = MasterShotBoundaryDecoder().newOperator("decode", input, context).toFlow(this).toList()
        val source = results.first()
        assertEquals("SOURCE:VIDEO", source.type)
        return results.filter { it.type == "SEGMENT" }.onEach { shot ->
            assertTrue(!shot.transient)
            assertEquals(source.id, shot.relationships.single().objectId)
        }
    }

    private fun range(shot: Retrievable) = shot.filteredAttribute(TimeRangeAttribute::class.java)
}
