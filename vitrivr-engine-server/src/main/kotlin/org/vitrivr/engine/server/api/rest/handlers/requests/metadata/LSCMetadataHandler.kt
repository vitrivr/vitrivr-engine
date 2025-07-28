package org.vitrivr.engine.server.api.rest.handlers.requests.metadata

import io.javalin.http.Context
import io.javalin.openapi.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * Route constant for the LSC metadata endpoint.
 */
const val LSC_METADATA_ROUTE =
    "metadata/{retrievableId}"

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class MetadataResponse(
    val timestamp: String? = null,
    val coordinates: Coordinates? = null
)

/**
 * Parses WKB (Well-Known Binary) hex string to extract coordinates.
 * PostGIS stores geometries as WKB in hex format.
 */
fun parseWKBHexToCoordinates(
    wkbHex: String
): Coordinates? {
    try {
        val cleanHex =
            wkbHex.trim()
                .uppercase()

        // Convert to byte array
        val bytes =
            ByteArray(
                cleanHex.length / 2
            ) { i ->
                cleanHex.substring(
                    i * 2,
                    i * 2 + 2
                )
                    .toInt(
                        16
                    )
                    .toByte()
            }

        val buffer =
            ByteBuffer.wrap(
                bytes
            )

        // Read byte order (1 = little endian, 0 = big endian)
        val byteOrder =
            buffer.get()
        if (byteOrder.toInt() == 1) {
            buffer.order(
                ByteOrder.LITTLE_ENDIAN
            )
        } else {
            buffer.order(
                ByteOrder.BIG_ENDIAN
            )
        }

        val geometryType =
            buffer.int
        val actualType =
            geometryType and 0xFF

        if (actualType != 1) {
            return null // Not a point geometry
        }

        // Check if SRID is present (0x20000000 flag)
        val hasSRID =
            (geometryType and 0x20000000) != 0
        if (hasSRID) {
            val srid =
                buffer.int // Skip SRID
        }

        // Read lon lat
        val longitude =
            buffer.double
        val latitude =
            buffer.double

        return Coordinates(
            latitude,
            longitude
        )

    } catch (e: Exception) {
        return null
    }
}

/**
 * A handler to fetch metadata for a specific retrievable.
 */
@OpenApi(
    path = "/api/{schema}/metadata/{retrievableId}",
    methods = [HttpMethod.GET],
    summary = "Fetches metadata for a retrievable.",
    operationId = "getMetadata",
    tags = ["Content"],
    pathParams = [
        OpenApiParam(
            "schema",
            type = String::class,
            description = "The schema this operation is for.",
            required = true
        ),
        OpenApiParam(
            "retrievableId",
            type = String::class,
            description = "The ID of the retrievable.",
            required = true
        )
    ],
    responses = [
        OpenApiResponse(
            "200",
            [OpenApiContent(
                Map::class
            )]
        ),
        OpenApiResponse(
            "400",
            [OpenApiContent(
                ErrorStatus::class
            )]
        ),
        OpenApiResponse(
            "404",
            [OpenApiContent(
                ErrorStatus::class
            )]
        )
    ]
)

        /**
         * Handles the request to fetch metadata for a specific retrievable.
         * Returns a simple string containing the lsctimestamp and postgiscoordinates.
         *
         * @param ctx The Javalin context.
         * @param schema The schema this operation is for.
         */
fun getMetadata(
    ctx: Context,
    schema: Schema
) {
    val logger =
        LoggerFactory.getLogger(
            "LSCMetadataHandler"
        )

    try {
        val retrievableIdString =
            ctx.pathParam(
                "retrievableId"
            )
        val retrievableId =
            UUID.fromString(
                retrievableIdString
            )

        //logger.debug("Fetching metadata for retrievable ID: {}", retrievableIdString)

        val lsctimestampField =
            schema.get(
                "lsctimestamp"
            )
        if (lsctimestampField == null) {
            ctx.status(
                404
            )
                .json(
                    ErrorStatus(
                        "Field 'lsctimestamp' not found in schema"
                    )
                )
            return
        }
        val lsctimestampReader =
            lsctimestampField.getReader()

        val postgiscoordinatesField =
            schema.get(
                "postgiscoordinates"
            )
        if (postgiscoordinatesField == null) {
            ctx.status(
                404
            )
                .json(
                    ErrorStatus(
                        "Field 'postgiscoordinates' not found in schema"
                    )
                )
            return
        }
        val postgiscoordinatesReader =
            postgiscoordinatesField.getReader()

        val lsctimestampDescriptor =
            lsctimestampReader.getForRetrievable(
                retrievableId
            )
                .firstOrNull() as? AnyMapStructDescriptor
        val postgiscoordinatesDescriptor =
            postgiscoordinatesReader.getForRetrievable(
                retrievableId
            )
                .firstOrNull() as? AnyMapStructDescriptor

        val timestamp =
            lsctimestampDescriptor?.values()
                ?.get(
                    "minuteIdTimestamp"
                ) as? Value.DateTime
        val coordinatesValue =
            postgiscoordinatesDescriptor?.values()
                ?.get(
                    "postgiscoordinates"
                ) as? Value.GeographyValue

        var parsedCoordinates: Coordinates? =
            null
        if (coordinatesValue != null) {

            val pointRegex =
                """POINT\(([-0-9.]+) ([-0-9.]+)\)""".toRegex()
            val wktMatch =
                pointRegex.find(
                    coordinatesValue.wkt
                )

            if (wktMatch != null) {
                val (longitude, latitude) = wktMatch.destructured
                parsedCoordinates =
                    Coordinates(
                        latitude.toDouble(),
                        longitude.toDouble()
                    )
                //logger.debug("Parsed WKT coordinates: lat={}, lon={}", latitude, longitude)
            } else {
                parsedCoordinates =
                    parseWKBHexToCoordinates(
                        coordinatesValue.wkt
                    )
                if (parsedCoordinates != null) {
                    //logger.debug("Parsed WKB coordinates: lat={}, lon={}", parsedCoordinates.latitude, parsedCoordinates.longitude)
                } else {
                    logger.warn(
                        "Failed to parse coordinates as WKT or WKB: '{}'",
                        coordinatesValue.wkt
                    )
                }
            }
        }

        // Build final response using the @Serializable MetadataResponse class
        val response =
            MetadataResponse(
                timestamp = timestamp?.value?.toString(),
                coordinates = parsedCoordinates
            )

        logger.debug(
            "Returning metadata: {}",
            response
        )
        //serialize this object into JSON
        ctx.json(
            response
        )

    } catch (e: Exception) {
        logger.error(
            "Error fetching metadata for retrievable ID: ${
                ctx.pathParam(
                    "retrievableId"
                )
            }",
            e
        )
        ctx.status(
            500
        )
            .json(
                ErrorStatus(
                    "Error fetching metadata: ${e.message}"
                )
            )
    }
}