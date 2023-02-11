package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.*

class GetDomainIT : PluginRestTestCase() {
    fun `test get domain`() {
        val createRequest = constructSchemaDomainRequest("testGetSchema")
        Thread.sleep(200)
        val objectId = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.OK.status
        ).get("objectId").asString
        Thread.sleep(200)
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain/$objectId",
            "",
            RestStatus.OK.status
        )
        val name = getResponse.get("name").asString
        Assert.assertEquals("Name should be present", "testGetSchema", name)
        Thread.sleep(200)
    }
}