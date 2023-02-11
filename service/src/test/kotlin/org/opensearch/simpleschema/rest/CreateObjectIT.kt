/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.*
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.BASE_SIMPLESCHEMA_URI
import org.opensearch.simpleschema.domain.DomainRepository
import org.opensearch.simpleschema.model.SimpleSchemaObjectType

class CreateObjectIT : PluginRestTestCase() {
    fun `test create schema fail`() {
        val invalidCreateResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            "",
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(invalidCreateResponse, RestStatus.BAD_REQUEST.status, "parse_exception")
        Thread.sleep(200)
    }

    fun `test create index provider`() {
        val createRequest = jsonify(constructIndexProviderRequest())
        createRequest.addProperty("objectId", "testId")

        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            createRequest.toString(),
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertEquals("testId", id)
        Thread.sleep(200)
    }

    fun `test create schema entity type`() {
        val createRequest = constructSchemaEntityTypeRequest()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("Id should be generated", id)
        Thread.sleep(200)
    }

    fun `test create object with invalid fields`() {
        val createRequest = """
            {
                "invalid-object": {
                    "name": "invalid"
                }
            }
        """.trimIndent()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            createRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(createResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
        Thread.sleep(200)
    }
}
