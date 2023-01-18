package org.opensearch.simpleschema.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.*
import org.opensearch.simpleschema.model.RestTag

class CreateDomainIT : PluginRestTestCase() {
    fun `test create schema domain type`() {
        val createRequest = constructSchemaEntityTypeRequest()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "${SimpleSchemaPlugin.BASE_SIMPLESCHEMA_URI}/domain",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("Id should be generated", id)
        Thread.sleep(100)
    }
}
