package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.openapi.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.rest.federation.*
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException

@OpenApi(
    path = "/api/{schema}/federation/lookup", methods = [HttpMethod.POST],
    summary = "Maps recording provenance and video-relative milliseconds to overlapping retrievables.",
    operationId = "postFederationLookup", tags = ["Federation"],
    pathParams = [OpenApiParam("schema", type = String::class, required = true)],
    requestBody = OpenApiRequestBody([OpenApiContent(FederationLookupRequest::class)]),
    responses = [OpenApiResponse("200", [OpenApiContent(FederationLookupResponse::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)]),
        OpenApiResponse("501", [OpenApiContent(ErrorStatus::class)])]
)
fun federationLookup(ctx: Context, schema: Schema) {
    val request = try { ctx.bodyAsClass<FederationLookupRequest>() } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid federation lookup request: ${e.message}")
    }
    try {
        ctx.json(FederationService(schema).lookup(request))
    } catch (e: IllegalArgumentException) {
        throw ErrorStatusException(400, e.message ?: "Invalid federation lookup request.")
    } catch (e: UnsupportedOperationException) {
        throw ErrorStatusException(501, "Federation lookup requires backend support for early metadata filtering.")
    }
}

@OpenApi(
    path = "/api/{schema}/federation/features", methods = [HttpMethod.POST],
    summary = "Reads stored ASR, OCR, identity and other configured feature values for a batch of retrievable IDs.",
    operationId = "postFederationFeatures", tags = ["Federation"],
    pathParams = [OpenApiParam("schema", type = String::class, required = true)],
    requestBody = OpenApiRequestBody([OpenApiContent(FederationFeaturesRequest::class)]),
    responses = [OpenApiResponse("200", [OpenApiContent(FederationFeaturesResponse::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])]
)
fun federationFeatures(ctx: Context, schema: Schema) {
    val request = try { ctx.bodyAsClass<FederationFeaturesRequest>() } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid federation feature request: ${e.message}")
    }
    try {
        ctx.json(FederationService(schema).features(request))
    } catch (e: IllegalArgumentException) {
        throw ErrorStatusException(400, e.message ?: "Invalid federation feature request.")
    }
}
