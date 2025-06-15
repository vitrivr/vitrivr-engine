package org.vitrivr.engine.index.features

import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadata
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.index.decode.ImageDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import org.vitrivr.engine.module.features.feature.lsc.coordinates.PostGISCoordinates
import kotlin.let
import kotlin.time.Duration


/**
 * An integration-style test for the [PostGISCoordinates] feature.
 *
 * This test runs a small pipeline to verify that the extractor correctly parses
 * GPS coordinates from different metadata sources in image files.
 *
 * @author henrikluemkemann
 * @version 1.0.0
 */
class PostGISCoordinatesTest {

    @Test
    fun test() = runTest(timeout = Duration.INFINITE) {

        val schema = Schema("test", BlackholeConnection("test", BlackholeConnectionProvider()))
        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))


        val contextConfig = IngestionContextConfig(
            contentFactory = "CachedContentFactory",
            resolvers = listOf("test"),
            global = emptyMap(),
            local = mapOf(
                "enumerator" to mapOf("path" to "./src/test/resources/CoordinateImages"),
                "decoder" to mapOf("timeWindowMs" to "1000")
            )
        )
        contextConfig.schema = schema

        //pipeline
        val context = IndexContextFactory.newContext(contextConfig)
        val fileSystemEnumerator = FileSystemEnumerator().newEnumerator("enumerator", context, listOf(MediaType.IMAGE))
        val decoder = ImageDecoder().newDecoder("decoder", input = fileSystemEnumerator, context = context)
        val postgiscoordinates = PostGISCoordinates().let {it.newExtractor(schema.Field("postgiscoordinates", it), input = decoder, context = context) }
        val file =  FileSourceMetadata().let { it.newExtractor(schema.Field("file", it), input = postgiscoordinates, context = context) }

        val results = file.toFlow(this).takeWhile { it != TerminalRetrievable }.toList()


        Assertions.assertEquals(3, results.size)
        Assertions.assertTrue(results.count { it.type == "SOURCE:IMAGE" } == 3)
        Assertions.assertTrue(results.all {it.relationships.isEmpty()})


        // Check each image's extracted data
        var exifFound = false
        var commentFound = false
        var noGpsFound = false

        for (r in results) {
            val fileDescriptor = r.descriptors.filterIsInstance<FileSourceMetadataDescriptor>().first()
            val coordinateDescriptor = r.descriptors.filterIsInstance<AnyMapStructDescriptor>().firstOrNull()
            val fileName = fileDescriptor.path.value.substringAfterLast("/")

            println("→ Processed file: $fileName")

            when (fileDescriptor.path.value.substringAfterLast("/")) {
                "image_with_exif_gps.jpeg" -> {
                    exifFound = true
                    assertNotNull(coordinateDescriptor, "EXIF GPS image should have a coordinate descriptor.")
                    val geoValue = coordinateDescriptor?.values()?.get("postgiscoordinates") as? Value.GeographyValue
                    println("EXIF PostGIS Coords: ${geoValue?.value}")
                    assertNotNull(geoValue, "GeographyValue should not be null for EXIF GPS.")
                    // Assert if the WKT string is correct
                    assertTrue(geoValue!!.value.contains("POINT(7.588561111111111 47.55985277777778)"), "WKT string from EXIF is incorrect.")
                }
                "image_with_comment_gps.jpg" -> {
                    commentFound = true
                    assertNotNull(coordinateDescriptor, "Comment GPS image should have a coordinate descriptor.")
                    val geoValue = coordinateDescriptor?.values()?.get("postgiscoordinates") as? Value.GeographyValue
                    println("Comment PostGIS Coords: ${geoValue?.value}")
                    assertNotNull(geoValue, "GeographyValue should not be null for comment GPS.")
                    assertTrue(geoValue!!.value.contains("POINT(-6.145789299992457 53.38998270000301)"), "WKT string from comment is incorrect.")
                }
                "image_no_gps.jpg" -> {
                    noGpsFound = true
                    println("No GPS metadata found for: $fileName")
                    assertNull(coordinateDescriptor, "Image with no GPS data should not have a coordinate descriptor.")
                }
                else -> {
                    println("Unexpected file encountered: $fileName")
                    fail("Unexpected file: ${fileDescriptor.path.value}")
                }
            }
        }

        assertTrue(exifFound, "Test image with EXIF GPS was not processed.")
        assertTrue(commentFound, "Test image with comment GPS was not processed.")
        assertTrue(noGpsFound, "Test image with no GPS was not processed.")
    }
}