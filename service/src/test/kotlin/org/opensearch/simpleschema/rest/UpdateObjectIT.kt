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
import org.opensearch.simpleschema.constructSchemaEntityTypeRequest
import org.opensearch.simpleschema.model.RestTag.OBJECT_LIST_FIELD

class UpdateObjectIT : PluginRestTestCase() {
    private fun createSchema(name: String = "test"): String {
        val createRequest = constructSchemaEntityTypeRequest(name)
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("id should be generated", id)
        Thread.sleep(200)
        return id
    }

    fun `test update invalid object`() {
        val updateRequest = constructSchemaEntityTypeRequest()
        val updateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_SIMPLESCHEMA_URI/object/does-not-exist",
            updateRequest,
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(updateResponse, RestStatus.NOT_FOUND.status)
    }

    fun `test update object`() {
        val id = createSchema()

        val newName = "updated_name"
        val updateRequest = constructSchemaEntityTypeRequest(newName)
        val updateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_SIMPLESCHEMA_URI/object/$id",
            updateRequest,
            RestStatus.OK.status
        )
        Assert.assertNotNull(id, updateResponse.get("objectId").asString)
        Thread.sleep(200)

        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object/$id",
            "",
            RestStatus.OK.status
        )
        val objectDetails = getResponse.get(OBJECT_LIST_FIELD).asJsonArray.get(0).asJsonObject
        Assert.assertEquals(id, objectDetails.get("objectId").asString)
        Assert.assertEquals(
            newName,
            objectDetails.get("schemaEntityType").asJsonObject.get("name").asString
        )
        Thread.sleep(200)
    }

    fun `test update object with invalid request`() {
        val id = createSchema()

        val updateRequest = """
            {
                "invalid-object": {
                    "name": "invalid"
                }
            }
        """.trimIndent()
        val updateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_SIMPLESCHEMA_URI/object/$id",
            updateRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(updateResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
        Thread.sleep(200)
    }
}
