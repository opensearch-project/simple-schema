package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.*

class CreateDomainIT : PluginRestTestCase() {
    fun `test create schema domain type`() {
        val createRequest = constructSchemaDomainRequest("sampleSchema")
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertEquals("Id should be present", "sampleSchema", id)
        Thread.sleep(100)
    }

    fun `test duplicate domain creation detection`() {
        val createRequest = constructSchemaDomainRequest("sampleSchema")
        executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.OK.status
        )
        executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.BAD_REQUEST.status
        )
        Thread.sleep(100)
    }

    fun `test domain compilation uses correct entities`() {
        val typeId = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/object",
            constructSchemaEntityTypeRequest(),
            RestStatus.OK.status
        ).get("objectId").asString
        val entities = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            constructSchemaDomainRequest(entities = "\"$typeId\""),
            RestStatus.OK.status
        ).get("entityList").asJsonArray.map { it.asString }.toList()
        Assert.assertEquals("Schema contains correct entities", entities, listOf(typeId))
        Thread.sleep(100)
    }
}
