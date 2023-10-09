package org.vitrivr.engine.base.features.external

import io.javalin.openapi.*
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.descriptor.Descriptor

/**
 * Interface representing external feature extraction definition for Vitrivr-Engine.
 * Communication with external feature extraction services should follow this contract.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
interface ExternalDefinition {

    /**
     * Creates a new extraction for a given element.
     *
     * @param featureName The name of the feature to be extracted.
     * @param modality Specification if feature supports several modalities (optional).
     * @return The extracted descriptors on success.
     * @throws ErrorStatus if the extraction fails or encounters an error.
     */
    @OpenApi(
        summary = "Creates a new extraction for a given element",
        path = "/extract/{featureName}/{modality}",
        operationId = "extract",
        methods = [HttpMethod.POST],
        pathParams = [
            OpenApiParam(
                name = "featureName",
                type = String::class,
                description = "The name of the feature to be extracted",
                required = true
            ),
            OpenApiParam(
                name = "modality",
                type = String::class,
                description = "Specification if feature supports several modalities",
                required = false
            )
        ],
        requestBody = OpenApiRequestBody([OpenApiContent(Content::class)]),
        responses = [
            OpenApiResponse("200", [OpenApiContent(Descriptor::class)]),
            OpenApiResponse("404", [OpenApiContent(ErrorStatus::class)]),
            OpenApiResponse("500", [OpenApiContent(ErrorStatus::class)]),
            OpenApiResponse("503", [OpenApiContent(ErrorStatus::class)]),
        ]
    )
    fun extract()
}
