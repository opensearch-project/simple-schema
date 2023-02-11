package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.*

class CreateDomainIT : PluginRestTestCase() {
    fun `test create schema domain type`() {
        val createRequest = constructSchemaDomainRequest("simpleSampleSchema")
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId")
        Assert.assertNotNull("Id should be present", id)
        Thread.sleep(200)
    }

    fun `test duplicate domain creation detection`() {
        executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            constructSchemaDomainRequest("duplicateSchema"),
            RestStatus.OK.status
        )
        executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            constructSchemaDomainRequest("duplicateSchema"),
            RestStatus.BAD_REQUEST.status
        )
        Thread.sleep(200)
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
            constructSchemaDomainRequest("schemaWithEntity", entities = "\"$typeId\""),
            RestStatus.OK.status
        ).get("entityList").asJsonArray.map { it.asString }.toList()
        Assert.assertEquals("Schema contains correct entities", entities, listOf(typeId))
        Thread.sleep(200)
    }

    fun `test domain compilation with missing entities fails`() {
        executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            constructSchemaDomainRequest("schemaWithInvalidEntities", entities = "\"invalidEntity\""),
            RestStatus.NOT_FOUND.status
        )
    }
}
