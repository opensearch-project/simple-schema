package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.*

class GetDomainIT : PluginRestTestCase() {
    fun `test get domain`() {
        val createRequest = constructSchemaDomainRequest("sampleSchema")
        executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.OK.status
        )
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain/sampleSchema",
            "",
            RestStatus.OK.status
        )
        logger.warn(getResponse.toString())
        val id = getResponse.get("objectId").asString
        Assert.assertEquals("Id should be present", "sampleSchema", id)
        Thread.sleep(100)
    }
}