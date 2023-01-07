package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.PluginRestTestCase
import org.opensearch.simpleschema.SimpleSchemaPlugin
import org.opensearch.simpleschema.constructSchemaCompilationTypeRequest
import org.opensearch.simpleschema.jsonify
import org.opensearch.simpleschema.model.RestTag

class CreateDomainIT : PluginRestTestCase() {
    private fun generateSchema(request: String): String {
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/object",
            request,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("Id should be generated", id)
        return id
    }

    fun `test create schema domain`() {
        val createRequest = constructSchemaCompilationTypeRequest()
        val id = generateSchema(createRequest)
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/object/$id/domain",
            "",
            RestStatus.OK.status
        )
        val objectDetails = getResponse.get(RestTag.OBJECT_LIST_FIELD).asJsonArray.get(0).asJsonObject
        Assert.assertEquals(id, objectDetails.get("objectId").asString)
        Assert.assertEquals(
            jsonify(createRequest).get("schemaCompilationType").asJsonObject,
            objectDetails.get("schemaCompilationType").asJsonObject
        )
        logger.info(objectDetails.get("schemaCompilationType").asJsonObject.toString())
        Thread.sleep(100)
    }
}
