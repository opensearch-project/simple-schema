/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.simpleschema.PluginRestTestCase
import org.opensearch.simpleschema.validateErrorResponse
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.BASE_SIMPLESCHEMA_URI
import org.opensearch.simpleschema.constructOntologyRequest

class DeleteObjectIT : PluginRestTestCase() {
    private fun createOntology(name: String = "test"): String {
        val createRequest = constructOntologyRequest(name)
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("ontologyId should be generated", id)
        Thread.sleep(100)
        return id
    }

    fun `test delete invalid ids`() {
        val invalidDeleteIdResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_SIMPLESCHEMA_URI/object/does-not-exist",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(invalidDeleteIdResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val invalidDeleteIdsResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectIdList=does-not-exist1,does-not-exist2",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(invalidDeleteIdsResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)
    }

    fun `test delete single object`() {
        val id = createOntology()
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_SIMPLESCHEMA_URI/object/$id",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(
            "OK",
            deleteResponse.get("deleteResponseList").asJsonObject.get(id).asString
        )
        Thread.sleep(100)
    }

    fun `test delete multiple objects`() {
        val ids: Set<String> = (1..20).map { createOntology() }.toSet()
        Thread.sleep(1000)
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectIdList=${ids.joinToString(separator = ",")}",
            "",
            RestStatus.OK.status
        )
        val deletedObject = deleteResponse.get("deleteResponseList").asJsonObject
        ids.forEach {
            Assert.assertEquals("OK", deletedObject.get(it).asString)
        }
        Thread.sleep(100)
    }
}
