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
import org.opensearch.simpleschema.model.RestTag.OBJECT_LIST_FIELD
import java.time.Instant

class GetObjectIT : PluginRestTestCase() {
    private fun createObject(createRequest: String): String {
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("Id should be generated", id)
        Thread.sleep(200)
        return id
    }

    fun `test get invalid ids`() {
        Thread.sleep(200)
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object/invalid-id",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(getResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(200)

        val getIdsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectIdList=invalid-id1,invalid-id2",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(getIdsResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(200)
    }

    fun `test get single object`() {
        val createRequest = constructSchemaEntityTypeRequest()
        val id = createObject(createRequest)

        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object/$id",
            "",
            RestStatus.OK.status
        )
        val objectDetails = getResponse.get(OBJECT_LIST_FIELD).asJsonArray.get(0).asJsonObject
        Assert.assertEquals(id, objectDetails.get("objectId").asString)
        Assert.assertEquals(
            jsonify(createRequest).get("schemaEntityType").asJsonObject,
            objectDetails.get("schemaEntityType").asJsonObject
        )
        Thread.sleep(200)
    }

    fun `test get multiple objects`() {
        Thread.sleep(200)
        val emptyResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(0, emptyResponse.get("totalHits").asInt)

        val startTime = Instant.now().toEpochMilli()
        val indexProviderIds = Array(6) { createObject(constructIndexProviderRequest("indexProvider-$it")) }
        val schemaEntityTypeIds =
            Array(3) { createObject(constructSchemaEntityTypeRequest("schemaEntityType-$it")) }
        val endTime = Instant.now().toEpochMilli()
        Thread.sleep(1000)

        val getAllResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object?maxItems=1000",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(9, getAllResponse.get("totalHits").asInt)

        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectType=schemaEntityType",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(3, getResponse.get("totalHits").asInt)
        val list = getResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            schemaEntityTypeIds,
            list.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )

        val getMultipleTypesResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectType=indexProvider,schemaEntityType",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(9, getMultipleTypesResponse.get("totalHits").asInt)
        var multipleTypesList = getMultipleTypesResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            indexProviderIds.plus(schemaEntityTypeIds),
            multipleTypesList.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )

        var getMultipleIdsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectIdList=${indexProviderIds.joinToString(",")}",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(6, getMultipleIdsResponse.get("totalHits").asInt)
        val multipleIdsList = getMultipleIdsResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            indexProviderIds,
            multipleIdsList.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )

        val getIndexProviderIdsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_SIMPLESCHEMA_URI/object?objectType=indexProvider",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(6, getIndexProviderIdsResponse.get("totalHits").asInt)
        multipleTypesList = getIndexProviderIdsResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            indexProviderIds,
            multipleTypesList.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )
    }
}
