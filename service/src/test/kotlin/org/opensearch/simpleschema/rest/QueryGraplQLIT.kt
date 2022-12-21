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

class QueryGraphQLIT : PluginRestTestCase() {
    fun `test get single object field`() {
        val createRequest = constructSchemaEntityTypeRequest()
        val id = "NQSM0CW"

        val getResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_SIMPLESCHEMA_URI/graphql",
            "{ \"body\": \"query { flights { flightNumber } }\" }",
            RestStatus.OK.status
        )
        val objectDetails = getResponse.get(OBJECT_LIST_FIELD).asJsonArray.get(0).asJsonObject
        Assert.assertEquals(id, objectDetails.get("flightNumber").asString)
        Assert.assertEquals(
            jsonify(createRequest).get("schemaEntityType").asJsonObject,
            objectDetails.get("schemaEntityType").asJsonObject
        )
        Thread.sleep(100)
    }
}
